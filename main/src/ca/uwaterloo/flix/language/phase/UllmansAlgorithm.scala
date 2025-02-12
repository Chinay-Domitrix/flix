/*
 * Copyright 2021 Jonathan Lindegaard Starup
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.uwaterloo.flix.language.phase

import ca.uwaterloo.flix.language.ast.{Ast, Name, SourceLocation, Type}
import ca.uwaterloo.flix.language.errors.StratificationError
import ca.uwaterloo.flix.util.Validation
import ca.uwaterloo.flix.util.Validation.{ToFailure, ToSuccess}

import scala.collection.mutable

object UllmansAlgorithm {


  /**
    * Represents a dependency between two predicate symbols.
    */
  sealed trait DependencyEdge

  object DependencyEdge {

    /**
      * Represents a positive labelled edge.
      */
    case class Positive(head: Name.Pred, body: Name.Pred, loc: SourceLocation) extends DependencyEdge

    /**
      * Represents a negative labelled edge.
      */
    case class Negative(head: Name.Pred, body: Name.Pred, loc: SourceLocation) extends DependencyEdge
  }

  /**
    * Represents the separated dependency graph oblivious to the rules that contain them.
    */
  type DependencyGraph = Set[DependencyEdge]

  /**
    * Computes the stratification of the given dependency graph `g` at the given source location `loc`.
    *
    * See Database and Knowledge - Base Systems Volume 1 Ullman, Algorithm 3.5 p 133
    */
  def stratify(g: DependencyGraph, tpe: Type, loc: SourceLocation): Validation[Ast.Stratification, StratificationError] = {
    //
    // Maintain a mutable map from predicates to their (maximum) stratum number.
    //
    // Any predicate not explicitly in the map has a default value of zero.
    //
    val stratumOf = mutable.Map.empty[Name.Pred, Int]

    //
    // Compute the number of dependency edges.
    //
    // The number of strata is bounded by the number of predicates which is bounded by the number of edges.
    //
    // Hence if we ever compute a stratum higher than this number then there is a negative cycle.
    //
    val maxStratum = g.size

    //
    // Repeatedly examine the dependency edges.
    //
    // We always consider two cases:
    //   1. A positive body predicate requires its head predicate to be in its stratum or any higher stratum.
    //   2. A negative body predicate requires its head predicate to be in a strictly higher stratum.
    //
    // If we ever create more strata than there are dependency edges then there is a negative cycle and we abort.
    //
    var changed = true
    while (changed) {
      changed = false

      // Examine each dependency edge in turn.
      for (edge <- g) {
        edge match {
          case DependencyEdge.Positive(headSym, bodySym, _) =>
            // Case 1: The stratum of the head must be in the same or a higher stratum as the body.
            val headStratum = stratumOf.getOrElseUpdate(headSym, 0)
            val bodyStratum = stratumOf.getOrElseUpdate(bodySym, 0)

            if (!(headStratum >= bodyStratum)) {
              // Put the head in the same stratum as the body.
              stratumOf.put(headSym, bodyStratum)
              changed = true
            }

          case DependencyEdge.Negative(headSym, bodySym, edgeLoc) =>
            // Case 2: The stratum of the head must be in a strictly higher stratum than the body.
            val headStratum = stratumOf.getOrElseUpdate(headSym, 0)
            val bodyStratum = stratumOf.getOrElseUpdate(bodySym, 0)

            if (!(headStratum > bodyStratum)) {
              // Put the head in one stratum above the body stratum.
              val newHeadStratum = bodyStratum + 1
              stratumOf.put(headSym, newHeadStratum)
              changed = true

              // Check if we have found a negative cycle.
              if (newHeadStratum > maxStratum) {
                return StratificationError(findNegativeCycle(bodySym, headSym, g, edgeLoc), tpe, loc).toFailure
              }
            }
        }
      }
    }

    // We are done. Successfully return the computed stratification.
    Ast.Stratification(stratumOf.toMap).toSuccess
  }

  /**
    * Returns a path that forms a cycle with the edge from `src` to `dst` in the given dependency graph `g`.
    */
  def findNegativeCycle(src: Name.Pred, dst: Name.Pred, g: DependencyGraph, loc: SourceLocation): List[(Name.Pred, SourceLocation)] = {
    // Computes a map from predicates to their successors.
    val succ = mutable.Map.empty[Name.Pred, Set[(Name.Pred, SourceLocation)]]
    for (edge <- g) {
      edge match {
        case DependencyEdge.Positive(head, body, loc) =>
          val s = succ.getOrElse(body, Set.empty)
          succ.put(body, s + ((head, loc)))
        case DependencyEdge.Negative(head, body, loc) =>
          val s = succ.getOrElse(body, Set.empty)
          succ.put(body, s + ((head, loc)))
      }
    }

    // We perform a DFS using recursion to find the cycle.

    // A map from predicates to their immediate predecessor in the DFS.
    val pred = mutable.Map.empty[Name.Pred, (Name.Pred, SourceLocation)]

    // A set of previously seen predicates.
    val seen = mutable.Set.empty[Name.Pred]

    // Recursively visit the given predicate.
    def visit(curr: Name.Pred): Unit = {
      // Update the set of previously seen nodes.
      seen.add(curr)

      // Recursively visit each unseen child.
      for ((succ, loc) <- succ.getOrElse(curr, Set.empty)) {
        if (!seen.contains(succ)) {
          pred.update(succ, (curr, loc))
          visit(succ)
        }
      }
    }

    // Compute the predecessor map.
    visit(dst)

    // Recursively constructs a path from `src` and backwards through the graph.
    def unroll(curr: Name.Pred): List[(Name.Pred, SourceLocation)] = pred.get(curr) match {
      case None => Nil
      case Some((prev, loc)) => (prev, loc) :: unroll(prev)
    }

    // Assemble the full path.
    (src, loc) :: unroll(src) ::: (src, loc) :: Nil
  }
}
