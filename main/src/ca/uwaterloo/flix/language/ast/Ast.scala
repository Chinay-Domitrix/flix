/*
 * Copyright 2015-2016 Magnus Madsen
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

package ca.uwaterloo.flix.language.ast

import java.nio.file.Path
import java.util.Objects

/**
  * A collection of AST nodes that are shared across multiple ASTs.
  */
object Ast {

  /**
    * A common super-type for inputs.
    */
  sealed trait Input

  object Input {

    /**
      * A source that is backed by an internal resource.
      */
    case class Internal(name: String, text: String) extends Input

    /**
      * A source that is backed by a regular string.
      */
    case class Str(text: String) extends Input

    /**
      * A source that is backed by a regular file.
      */
    case class TxtFile(path: Path) extends Input

    /**
      * A source that is backed by flix package file.
      */
    case class PkgFile(path: Path) extends Input

  }

  /**
    * A source is a name and an array of character data.
    */
  case class Source(name: String, data: Array[Char]) {
    def format: String = name

    override def equals(o: scala.Any): Boolean = o match {
      case that: Source => this.name == that.name
    }

    override def hashCode(): Int = name.hashCode
  }

  /**
    * A common super type for AST nodes that represent annotations.
    */
  trait Annotation

  object Annotation {

    /**
      * An AST node that represents a `@benchmark` annotation.
      *
      * A function marked with `benchmark` is evaluated as part of the benchmark framework.
      *
      * @param loc the source location of the annotation.
      */
    case class Benchmark(loc: SourceLocation) extends Annotation {
      override def toString: String = "@benchmark"
    }

    /**
      * An AST node that represents a `@deprecated` annotation.
      *
      * @param loc the source location of the annotation.
      */
    case class Deprecated(loc: SourceLocation) extends Annotation {
      override def toString: String = "@deprecated"
    }

    /**
      * An AST node that represents a `@law` annotation.
      *
      * A `law` function is a property (theorem) about the behaviour of one or more functions.
      *
      * @param loc the source location of the annotation.
      */
    case class Law(loc: SourceLocation) extends Annotation {
      override def toString: String = "@law"
    }

    /**
      * An AST node that represents a `@lint` annotation.
      *
      * @param loc the source location of the annotation.
      */
    case class Lint(loc: SourceLocation) extends Annotation {
      override def toString: String = "@lint"
    }

    /**
      * An AST node that represents a `@test` annotation.
      *
      * A function marked with `test` is evaluated as part of the test framework.
      *
      * @param loc the source location of the annotation.
      */
    case class Test(loc: SourceLocation) extends Annotation {
      override def toString: String = "@test"
    }

    /**
      * An AST node that represents a `@LazyWhenPure` annotation.
      *
      * @param loc the source location of the annotation.
      */
    case class LazyWhenPure(loc: SourceLocation) extends Annotation {
      override def toString: String = "@LazyWhenPure"
    }

    /**
      * An AST node that represents a `@ParallelWhenPure` annotation.
      *
      * @param loc the source location of the annotation.
      */
    case class ParallelWhenPure(loc: SourceLocation) extends Annotation {
      override def toString: String = "@ParallelWhenPure"
    }

    /**
      * An AST node that represents a `@Time` annotation.
      *
      * @param loc the source location of the annotation.
      */
    case class Time(loc: SourceLocation) extends Annotation {
      override def toString: String = "@Time"
    }

    /**
      * An AST node that represents a `@Space` annotation.
      *
      * @param loc the source location of the annotation.
      */
    case class Space(loc: SourceLocation) extends Annotation {
      override def toString: String = "@Space"
    }

  }

  /**
    * Companion object of [[Annotations]].
    */
  object Annotations {
    /**
      * The empty sequence of annotations.
      */
    val Empty: Annotations = Annotations(Nil)
  }

  /**
    * A sequence of annotations.
    */
  case class Annotations(annotations: List[Annotation]) {

    /**
      * Returns `true` if `this` sequence contains the `@benchmark` annotation.
      */
    def isBenchmark: Boolean = annotations exists (_.isInstanceOf[Annotation.Benchmark])

    /**
      * Returns `true` if `this` sequence contains the `@law` annotation.
      */
    def isLaw: Boolean = annotations exists (_.isInstanceOf[Annotation.Law])

    /**
      * Returns `true` if `this` sequence contains the `@lint` annotation.
      */
    def isLint: Boolean = annotations exists (_.isInstanceOf[Annotation.Lint])

    /**
      * Returns `true` if `this` sequence contains the `@test` annotation.
      */
    def isTest: Boolean = annotations exists (_.isInstanceOf[Annotation.Test])

  }

  /**
    * Documentation.
    *
    * @param lines the lines of the comments.
    * @param loc   the source location of the text.
    */
  case class Doc(lines: List[String], loc: SourceLocation) {
    def text: String = lines.
      dropWhile(_.trim.isEmpty).
      map(_.trim).
      mkString("\n")
  }

  /**
    * Companion object of [[Modifiers]].
    */
  object Modifiers {
    /**
      * The empty sequence of modifiers.
      */
    val Empty: Modifiers = Modifiers(Nil)
  }

  /**
    * A sequence of modifiers.
    */
  case class Modifiers(mod: List[Modifier]) {
    /**
      * Returns `true` if these modifiers contain the inline modifier.
      */
    def isInline: Boolean = mod contains Modifier.Inline

    /**
      * Returns `true` if these modifiers contain the lawless modifier.
      */
    def isLawless: Boolean = mod contains Modifier.Lawless

    /**
      * Returns `true` if these modifiers contain the override modifier.
      */
    def isOverride: Boolean = mod contains Modifier.Override

    /**
      * Returns `true` if these modifiers contain the public modifier.
      */
    def isPublic: Boolean = mod contains Modifier.Public

    /**
      * Returns `true` if these modifiers contain the scoped modifier.
      */
    def isScoped: Boolean = mod contains Modifier.Scoped

    /**
      * Returns `true` if these modifiers contain the sealed modifier.
      */
    def isSealed: Boolean = mod contains Modifier.Sealed

    /**
      * Returns `true` if these modifiers contain the synthetic modifier.
      */
    def isSynthetic: Boolean = mod contains Modifier.Synthetic

    /**
      * Returns `true` if these modifiers contain the unlawful modifier.
      */
    def isUnlawful: Boolean = mod contains Modifier.Unlawful

  }

  /**
    * A common super-type for modifiers.
    */
  sealed trait Modifier

  object Modifier {

    /**
      * The inline modifier.
      */
    case object Inline extends Modifier

    /**
      * The lawless modifier.
      */
    case object Lawless extends Modifier

    /**
      * The override modifier.
      */
    case object Override extends Modifier

    /**
      * The public modifier.
      */
    case object Public extends Modifier

    /**
      * The scoped modifier.
      */
    case object Scoped extends Modifier

    /**
      * The sealed modifier.
      */
    case object Sealed extends Modifier

    /**
      * The synthetic modifier.
      */
    case object Synthetic extends Modifier

    /**
      * The unlawful modifier.
      */
    case object Unlawful extends Modifier

  }

  /**
    * A common super-type for the denotation of an atom.
    */
  sealed trait Denotation

  object Denotation {

    /**
      * The atom has a relational denotation.
      */
    case object Relational extends Denotation

    /**
      * The atom has a latticenal denotation.
      */
    case object Latticenal extends Denotation

  }

  /**
    * A common super-type for the polarity of an atom.
    */
  sealed trait Polarity

  object Polarity {

    /**
      * The atom is positive.
      */
    case object Positive extends Polarity

    /**
      * The atom is negative.
      */
    case object Negative extends Polarity

  }

  /**
    * Contains the edges held in a single constraint.
    */
  case class MultiEdge(head: (Name.Pred, Type), positives: Vector[TypedPredicate], negatives: Vector[TypedPredicate])

  /**
    * Represents a body predicate on a MultiEdge.
    */
  case class TypedPredicate(atom: Name.Pred, tpe: Type, loc: SourceLocation)

  object ConstraintGraph {
    /**
      * The empty constraint graph.
      */
    val empty: ConstraintGraph = ConstraintGraph(Set.empty)
  }

  /**
    * Represents a constraint graph; a set dependency graphs representing each rule.
    */
  case class ConstraintGraph(xs: Set[MultiEdge]) {
    /**
      * Returns a constraint graph with all dependency graphs in `this` and `that` constraint graph.
      */
    def +(that: ConstraintGraph): ConstraintGraph = {
      if (this eq ConstraintGraph.empty)
        that
      else if (that eq ConstraintGraph.empty)
        this
      else
        ConstraintGraph(this.xs ++ that.xs)
    }

    /**
      * Returns `this` constraint graph including only the edges where all
      * its predicates is in `syms` and `typeEquality` returns true for the predicate type and its type in `syms`.
      * A rule like
      * `A(ta) :- B(tb), C(tc), not D(tc).` is represented by `MultiEdge((A, ta), {(B, tb), (C, tc)}, {(D, td)})`
      * and is only included in the output if `syms` contains all of `A, B, C, D` and `typeEquality(syms(A), ta)` etc.
      */
    def restrict(syms: Map[Name.Pred, Type], typeEquality: (Type, Type) => Boolean): ConstraintGraph =
      ConstraintGraph(xs.filter {
        case MultiEdge((head, headTpe), positives, negatives) =>
          def checkBody(e: TypedPredicate): Boolean = e match {
            case TypedPredicate(s, tpe, _) => syms.get(s).exists(typeEquality(_, tpe))
          }

          syms.get(head).exists(typeEquality(_, headTpe)) &&
            positives.forall(checkBody) && negatives.forall(checkBody)
      })
  }

  object Stratification {
    /**
      * Represents the empty stratification.
      */
    val Empty: Stratification = Stratification(Map.empty)
  }

  /**
    * Represents a stratification that maps every predicate symbol to its stratum.
    */
  case class Stratification(m: Map[Name.Pred, Int])

  /**
    * A hole context consists of a hole symbol and its type together with the local environment.
    */
  case class HoleContext(sym: Symbol.HoleSym, tpe: Type, env: Map[Symbol.VarSym, Type])

  /**
    * Represents that the annotated element is introduced by the class `clazz`.
    */
  case class IntroducedBy(clazz: java.lang.Class[_]) extends scala.annotation.StaticAnnotation

  /**
    * Represents that the annotated element is eliminated by the class `clazz`.
    */
  case class EliminatedBy(clazz: java.lang.Class[_]) extends scala.annotation.StaticAnnotation

  /**
    * Represents that the type `arg` must belong to class `sym`.
    */
  case class TypeConstraint(sym: Symbol.ClassSym, arg: Type, loc: SourceLocation) {
    override def equals(o: Any): Boolean = o match {
      case that: TypeConstraint =>
        this.sym == that.sym && this.arg == that.arg
      case _ => false
    }

    override def hashCode(): Int = Objects.hash(sym, arg)
  }

  /**
    * Represents that an instance on type `tpe` has the type constraints `tconstrs`.
    */
  case class Instance(tpe: Type, tconstrs: List[Ast.TypeConstraint])

  /**
    * Represents the super classes and instances available for a particular class.
    */
  case class ClassContext(superClasses: List[Symbol.ClassSym], instances: List[Ast.Instance])

  /**
    * Represents a derivation on an enum (e.g. `enum E with Eq`).
    */
  case class Derivation(clazz: Symbol.ClassSym, loc: SourceLocation)

  /**
    * Represents the way a variable is bound.
    */
  sealed trait BoundBy

  object BoundBy {

    /**
      * Represents a variable that is bound by a formal parameter.
      */
    case object FormalParam extends BoundBy

    /**
      * Represents a variable that is bound by a let-binding.
      */
    case object Let extends BoundBy

    /**
      * Represents a variable that is bound by a pattern.
      */
    case object Pattern extends BoundBy

    /**
      * Represents a variable that is bound by a select.
      */
    case object SelectRule extends BoundBy

    /**
      * Represents a variable that is bound by a catch rule.
      */
    case object CatchRule extends BoundBy

    /**
      * Represents a variable that is bound by a constraint.
      */
    case object Constraint extends BoundBy

  }

}
