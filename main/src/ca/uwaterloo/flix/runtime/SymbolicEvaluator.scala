package ca.uwaterloo.flix.runtime

import ca.uwaterloo.flix.language.ast._
import ca.uwaterloo.flix.language.ast.ExecutableAst
import ca.uwaterloo.flix.language.ast.ExecutableAst.Expression
import ca.uwaterloo.flix.language.ast.ExecutableAst.Expression.Ref
import ca.uwaterloo.flix.language.phase.GenSym
import ca.uwaterloo.flix.util.InternalCompilerException

import scala.collection.mutable

object SymbolicEvaluator {

  sealed trait Expr {
    def tpe: Type
  }

  object Expr {

    /**
      * Int8 expression.
      */
    case class Int8(lit: Byte) extends Expr {
      def tpe: Type = Type.Int8
    }

    /**
      * Int16 expression.
      */
    case class Int16(lit: Short) extends Expr {
      def tpe: Type = Type.Int16
    }

    /**
      * Int32 expression.
      */
    case class Int32(lit: Int) extends Expr {
      def tpe: Type = Type.Int32
    }

    /**
      * Int64 expression.
      */
    case class Int64(lit: Long) extends Expr {
      def tpe: Type = Type.Int64
    }

    /**
      * Variables expression.
      */
    case class Var(ident: Name.Ident, tpe: Type) extends Expr


    case class Plus(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class Minus(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class Times(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class Divide(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class Modulo(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class Exponentiate(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class Less(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = Type.Bool
    }

    case class LessEqual(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = Type.Bool
    }

    case class Greater(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = Type.Bool
    }

    case class GreaterEqual(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = Type.Bool
    }

    case class Equal(e1: Expr, e2: Expr) extends Expr {
      assert(e1.tpe == e2.tpe)

      def tpe: Type = Type.Bool
    }

    case class NotEqual(e1: Expr, e2: Expr) extends Expr {
      assert(e1.tpe == e2.tpe)

      def tpe: Type = Type.Bool
    }

    case class Not(e: Expr) extends Expr {
      assert(e.tpe == Type.Bool)

      def tpe: Type = Type.Bool
    }

    case class LogicalAnd(e1: Expr, e2: Expr) extends Expr {
      assert(e1.tpe == Type.Bool && e2.tpe == Type.Bool)

      def tpe: Type = Type.Bool
    }

    case class LogicalOr(e1: Expr, e2: Expr) extends Expr {
      assert(e1.tpe == Type.Bool && e2.tpe == Type.Bool)

      def tpe: Type = Type.Bool
    }

    case class Implication(e1: Expr, e2: Expr) extends Expr {
      assert(e1.tpe == Type.Bool && e2.tpe == Type.Bool)

      def tpe: Type = Type.Bool
    }

    case class Bicondition(e1: Expr, e2: Expr) extends Expr {
      assert(e1.tpe == Type.Bool && e2.tpe == Type.Bool)

      def tpe: Type = Type.Bool
    }

    case class BitwiseNegate(e: Expr) extends Expr {
      assert(numeric(e.tpe))

      def tpe: Type = e.tpe
    }

    case class BitwiseAnd(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class BitwiseOr(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class BitwiseXor(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class BitwiseLeftShift(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

    case class BitwiseRightShift(e1: Expr, e2: Expr) extends Expr {
      assert(numeric(common(e1.tpe, e2.tpe)))

      def tpe: Type = common(e1.tpe, e2.tpe)
    }

  }

  /**
    * Symbolic Values.
    *
    * A symbolic value is like a regular value but allows atomic variables.
    */
  sealed trait SymVal

  object SymVal {

    /**
      * An atomic symbolic variable.
      *
      * @param ident the identifier.
      */
    case class AtomicVar(ident: Name.Ident) extends SymVal

    /**
      * The `Unit` value.
      */
    case object Unit extends SymVal

    /**
      * The `True` value.
      */
    case object True extends SymVal

    /**
      * The `False` value.
      */
    case object False extends SymVal

    /**
      * A Char value.
      */
    case class Char(lit: Int) extends SymVal

    /**
      * A Float32 value.
      */
    case class Float32(lit: Float) extends SymVal

    /**
      * A Float64 value.
      */
    case class Float64(lit: Double) extends SymVal

    /**
      * An Int8 value.
      *
      * @param lit the int literal.
      */
    case class Int8(lit: Byte) extends SymVal

    /**
      * An Int16 value.
      *
      * @param lit the int literal.
      */
    case class Int16(lit: Short) extends SymVal

    /**
      * An Int32 value.
      *
      * @param lit the int literal.
      */
    case class Int32(lit: Int) extends SymVal

    /**
      * An Int64 value.
      *
      * @param lit the int literal.
      */
    case class Int64(lit: Long) extends SymVal

    /**
      * A String value.
      *
      * @param lit the int literal.
      */
    case class Str(lit: String) extends SymVal

    /**
      * A tag value.
      *
      * @param tag   the tag name.
      * @param value the tagged value.
      */
    case class Tag(tag: String, value: SymVal) extends SymVal

    /**
      * A tuple value.
      *
      * @param elms the elements of the tuple.
      */
    case class Tuple(elms: List[SymVal]) extends SymVal

    /**
      * A closure value.
      *
      * @param exp    the nexpression of the closure.
      * @param cloVar the name of the closure variable.
      * @param env    the closure environment.
      */
    case class Closure(exp: Expression, cloVar: String, env: Environment) extends SymVal

    /**
      * An environment for closure variables.
      *
      * @param m the map from closure variables to symbolic values.
      */
    case class Environment(m: Map[String, SymVal]) extends SymVal

    /**
      * A user error value.
      */
    case class UserError(loc: SourceLocation) extends SymVal

    /**
      * A match error value.
      */
    case class MatchError(loc: SourceLocation) extends SymVal

    /**
      * A switch error value.
      */
    case class SwitchError(loc: SourceLocation) extends SymVal

  }

  /**
    * The type of path constraints.
    */
  type PathConstraint = List[Expr]

  /**
    * The type of environments.
    */
  type Environment = mutable.Map[String, SymVal] // TODO: Consider immutable map

  // TODO: Consider ContextType

  /**
    * Evaluates the given expression `exp0` under the given environment `env0`.
    */
  def eval(exp0: Expression, env0: Map[String, Expression], root: ExecutableAst.Root)(implicit genSym: GenSym): List[(PathConstraint, SymVal)] = {
    /*
      * Local visitor.
      */
    def eval(pc0: PathConstraint, exp0: Expression, env0: Environment)(implicit genSym: GenSym): List[(PathConstraint, SymVal)] = exp0 match {
      /**
        * Unit.
        */
      case Expression.Unit => lift(pc0, SymVal.Unit)

      /**
        * True.
        */
      case Expression.True => lift(pc0, SymVal.True)

      /**
        * False.
        */
      case Expression.False => lift(pc0, SymVal.False)

      /**
        * Char.
        */
      case Expression.Char(lit) => lift(pc0, SymVal.Char(lit))

      /**
        * Float32.
        */
      case Expression.Float32(lit) => lift(pc0, SymVal.Float32(lit))

      /**
        * Float64.
        */
      case Expression.Float64(lit) => lift(pc0, SymVal.Float64(lit))

      /**
        * Int8.
        */
      case Expression.Int8(lit) => lift(pc0, SymVal.Int8(lit))

      /**
        * Int16.
        */
      case Expression.Int16(lit) => lift(pc0, SymVal.Int16(lit))

      /**
        * Int32.
        */
      case Expression.Int32(lit) => lift(pc0, SymVal.Int32(lit))

      /**
        * Int64.
        */
      case Expression.Int64(lit) => lift(pc0, SymVal.Int64(lit))

      /**
        * Str.
        */
      case Expression.Str(lit) => lift(pc0, SymVal.Str(lit))

      /**
        * Local Variable.
        */
      case Expression.Var(ident, _, tpe, loc) => lift(pc0, env0(ident.name))

      /**
        * Closure Variable.
        */
      case Expression.ClosureVar(env, name, _, _) =>
        // Lookup the closure environment.
        val SymVal.Environment(m) = env0(env.name)
        // Lookup the variable in the closure environment.
        lift(pc0, m(name.name))

      /**
        * Reference.
        */
      case Expression.Ref(name, tpe, loc) =>
        // Lookup and evaluate the definition.
        root.constants.get(name) match {
          case None => throw InternalCompilerException(s"Type Error: Unresolved reference '$name'.")
          case Some(defn) => eval(pc0, defn.exp, env0)
        }

      // TODO: Document
      case Expression.ApplyRef(name, args, _, _) =>
        val defn = root.constants(name)
        evaln(pc0, args, env0) flatMap {
          case (pc, as) =>
            val newEnv = mutable.Map.empty[String, SymVal]
            for ((formal, actual) <- defn.formals zip as) {
              newEnv += (formal.ident.name -> actual)
            }
            eval(pc, defn.exp, newEnv)
        }
      // TODO: Document
      case Expression.ApplyClosure(exp, args, _, _) =>
        eval(pc0, exp, env0) flatMap {
          case (pc, SymVal.Closure(cloExp, cloVar, cloEnv)) =>
            val newEnv = mutable.Map.empty[String, SymVal]
            newEnv += (cloVar -> cloEnv)
            eval(pc, cloExp, newEnv)
          case (_, v) => throw InternalCompilerException(s"Type Error: Unexpected value: '$v'.")
        }

      // TODO: Document
      case Expression.MkClosure(lambda, cloVar, freeVars, _, _) =>
        val closureEnv = mutable.Map.empty[String, SymVal]
        for (freeVar <- freeVars) {
          closureEnv += (freeVar.name -> env0(freeVar.name))
        }
        val cloVal = SymVal.Closure(lambda.asInstanceOf[Ref], cloVar.name, SymVal.Environment(closureEnv.toMap))
        lift(pc0, cloVal)

      /**
        * Unary.
        */
      case Expression.Unary(op, exp, _, _) =>
        eval(pc0, exp, env0) flatMap {
          case (pc, v) => op match {
            /**
              * Unary Not.
              */
            case UnaryOperator.LogicalNot => v match {
              // Concrete semantics.
              case SymVal.True => lift(pc, SymVal.False)
              case SymVal.False => lift(pc, SymVal.True)

              // Symbolic Semantics.
              case SymVal.AtomicVar(id) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.False),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.True)
              )

              case _ => throw InternalCompilerException(s"Type Error: Unexpected value: '$v'.")
            }

            /**
              * Unary Plus.
              */
            case UnaryOperator.Plus => lift(pc, v)

            /**
              * Unary Minus.
              */
            case UnaryOperator.Minus => v match {
              // Concrete semantics.
              case SymVal.Int8(i) => lift(pc, SymVal.Int8((-i).toByte))
              case SymVal.Int16(i) => lift(pc, SymVal.Int16((-i).toShort))
              case SymVal.Int32(i) => lift(pc, SymVal.Int32(-i))
              case SymVal.Int64(i) => lift(pc, SymVal.Int64(-i))

              // Symbolic semantics.
              case SymVal.AtomicVar(id) =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.Minus(zeroOf(exp.tpe), Expr.Var(id, exp.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))

              case _ => throw InternalCompilerException(s"Type Error: Unexpected value: '$v'.")
            }

            /**
              * Unary Bitwise Negate.
              */
            case UnaryOperator.BitwiseNegate => v match {
              // Concrete semantics
              case SymVal.Int8(i) => lift(pc, SymVal.Int8((~i).toByte))
              case SymVal.Int16(i) => lift(pc, SymVal.Int16((~i).toShort))
              case SymVal.Int32(i) => lift(pc, SymVal.Int32(~i))
              case SymVal.Int64(i) => lift(pc, SymVal.Int64(~i))

              // Symbolic semantics
              case SymVal.AtomicVar(id) =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.BitwiseNegate(Expr.Var(id, exp.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))

              case _ => throw InternalCompilerException(s"Type Error: Unexpected value: '$v'.")
            }

          }
        }

      /**
        * Binary.
        */
      case Expression.Binary(op, exp1, exp2, _, _) =>
        eval2(pc0, exp1, exp2, env0) flatMap {
          case (pc, (v1, v2)) => op match {

            /**
              * Plus.
              */
            case BinaryOperator.Plus => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 + i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 + i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 + i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 + i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.Plus(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Minus.
              */
            case BinaryOperator.Minus => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 - i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 - i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 - i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 - i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.Minus(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Times.
              */
            case BinaryOperator.Times => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 * i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 * i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 * i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 * i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.Times(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Divide.
              */
            case BinaryOperator.Divide => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 / i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 / i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 / i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 / i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.Divide(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Modulo.
              */
            case BinaryOperator.Modulo => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 % i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 % i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 % i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 % i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.Modulo(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Exponentiate.
              */
            case BinaryOperator.Exponentiate => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8(Math.pow(i1, i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16(Math.pow(i1, i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(Math.pow(i1, i2).toInt))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(Math.pow(i1, i2).toLong))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.Exponentiate(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Less.
              */
            case BinaryOperator.Less => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, toBool(i1 < i2))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, toBool(i1 < i2))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, toBool(i1 < i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, toBool(i1 < i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, Type.Bool), Expr.Less(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * LessEqual.
              */
            case BinaryOperator.LessEqual => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, toBool(i1 <= i2))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, toBool(i1 <= i2))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, toBool(i1 <= i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, toBool(i1 <= i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, Type.Bool), Expr.LessEqual(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Greater.
              */
            case BinaryOperator.Greater => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, toBool(i1 > i2))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, toBool(i1 > i2))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, toBool(i1 > i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, toBool(i1 > i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, Type.Bool), Expr.Greater(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * GreaterEqual.
              */
            case BinaryOperator.GreaterEqual => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, toBool(i1 >= i2))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, toBool(i1 >= i2))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, toBool(i1 >= i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, toBool(i1 >= i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, Type.Bool), Expr.GreaterEqual(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Equal.
              */
            case BinaryOperator.Equal => eq(pc, v1, v2, exp1.tpe)

            /**
              * Not Equal.
              */
            case BinaryOperator.NotEqual => eq(pc, v1, v2, exp1.tpe).flatMap {
              case (pc1, SymVal.True) => lift(pc1, SymVal.False)
              case (pc1, SymVal.False) => lift(pc1, SymVal.True)
              case (_, v) => throw InternalCompilerException(s"Type Error: Unexpected value:'$v'.")
            }

            /**
              * Logical And.
              */
            case BinaryOperator.LogicalAnd => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.True, SymVal.True) => lift(pc, SymVal.True)
              case (SymVal.True, SymVal.False) => lift(pc, SymVal.False)
              case (SymVal.False, SymVal.True) => lift(pc, SymVal.False)
              case (SymVal.False, SymVal.False) => lift(pc, SymVal.False)

              // Symbolic semantics.
              case (SymVal.True, SymVal.AtomicVar(id)) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.True),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.False)
              )
              case (SymVal.AtomicVar(id), SymVal.True) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.True),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.False)
              )

              case (SymVal.False, SymVal.AtomicVar(id)) => lift(pc, SymVal.False)
              case (SymVal.AtomicVar(id), SymVal.False) => lift(pc, SymVal.False)

              case (SymVal.AtomicVar(id1), SymVal.AtomicVar(id2)) => List(
                (Expr.LogicalAnd(Expr.Var(id1, Type.Bool), Expr.Var(id2, Type.Bool)) :: pc, SymVal.True),
                (Expr.Not(Expr.LogicalAnd(Expr.Var(id1, Type.Bool), Expr.Var(id2, Type.Bool))) :: pc, SymVal.False)
              )

              case _ => throw InternalCompilerException(s"Type Error: Unexpected expression: '$v1 && $v2'.")
            }

            /**
              * Logical Or.
              */
            case BinaryOperator.LogicalOr => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.True, SymVal.True) => lift(pc, SymVal.True)
              case (SymVal.True, SymVal.False) => lift(pc, SymVal.True)
              case (SymVal.False, SymVal.True) => lift(pc, SymVal.True)
              case (SymVal.False, SymVal.False) => lift(pc, SymVal.False)

              // Symbolic semantics.
              case (SymVal.True, SymVal.AtomicVar(id)) => lift(pc, SymVal.True)
              case (SymVal.AtomicVar(id), SymVal.True) => lift(pc, SymVal.True)

              case (SymVal.False, SymVal.AtomicVar(id)) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.True),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.False)
              )
              case (SymVal.AtomicVar(id), SymVal.False) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.True),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.False)
              )

              case (SymVal.AtomicVar(id1), SymVal.AtomicVar(id2)) => List(
                (Expr.LogicalOr(Expr.Var(id1, Type.Bool), Expr.Var(id2, Type.Bool)) :: pc, SymVal.True),
                (Expr.Not(Expr.LogicalOr(Expr.Var(id1, Type.Bool), Expr.Var(id2, Type.Bool))) :: pc, SymVal.False)
              )

              case _ => throw InternalCompilerException(s"Type Error: Unexpected expression: '$v1 || $v2'.")
            }

            /**
              * Logical Implication.
              */
            case BinaryOperator.Implication => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.True, SymVal.True) => lift(pc, SymVal.True)
              case (SymVal.True, SymVal.False) => lift(pc, SymVal.False)
              case (SymVal.False, SymVal.True) => lift(pc, SymVal.True)
              case (SymVal.False, SymVal.False) => lift(pc, SymVal.True)

              // Symbolic semantics.
              case (SymVal.True, SymVal.AtomicVar(id)) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.True),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.False)
              )
              case (SymVal.False, SymVal.AtomicVar(id)) => lift(pc, SymVal.True)

              case (SymVal.AtomicVar(id), SymVal.False) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.False),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.True)
              )
              case (SymVal.AtomicVar(id), SymVal.True) => lift(pc, SymVal.True)

              case (SymVal.AtomicVar(id1), SymVal.AtomicVar(id2)) => List(
                (Expr.Implication(Expr.Var(id1, Type.Bool), Expr.Var(id2, Type.Bool)) :: pc, SymVal.True),
                (Expr.Not(Expr.Implication(Expr.Var(id1, Type.Bool), Expr.Var(id2, Type.Bool))) :: pc, SymVal.True)
              )

              case _ => throw InternalCompilerException(s"Type Error: Unexpected expression: '$v1 => $v2'.")
            }

            /**
              * Logical Bicondition.
              */
            case BinaryOperator.Biconditional => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.True, SymVal.True) => lift(pc, SymVal.True)
              case (SymVal.True, SymVal.False) => lift(pc, SymVal.False)
              case (SymVal.False, SymVal.True) => lift(pc, SymVal.False)
              case (SymVal.False, SymVal.False) => lift(pc, SymVal.True)

              // Symbolic semantics.
              case (SymVal.True, SymVal.AtomicVar(id)) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.True),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.False)
              )
              case (SymVal.AtomicVar(id), SymVal.True) => List(
                (Expr.Var(id, Type.Bool) :: pc, SymVal.True),
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.False)
              )

              case (SymVal.False, SymVal.AtomicVar(id)) => List(
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.True),
                (Expr.Var(id, Type.Bool) :: pc, SymVal.False)
              )
              case (SymVal.AtomicVar(id), SymVal.False) => List(
                (Expr.Not(Expr.Var(id, Type.Bool)) :: pc, SymVal.True),
                (Expr.Var(id, Type.Bool) :: pc, SymVal.False)
              )

              case (SymVal.AtomicVar(id1), SymVal.AtomicVar(id2)) => List(
                (Expr.Bicondition(Expr.Var(id1, Type.Bool), Expr.Var(id2, Type.Bool)) :: pc, SymVal.True),
                (Expr.Not(Expr.Bicondition(Expr.Var(id1, Type.Bool), Expr.Var(id2, Type.Bool))) :: pc, SymVal.False)
              )

              case _ => throw InternalCompilerException(s"Type Error: Unexpected expression: '$v1 <==> $v2'.")
            }

            /**
              * Bitwise And.
              */
            case BinaryOperator.BitwiseAnd => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 & i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 & i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 & i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 & i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.BitwiseAnd(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Bitwise Or.
              */
            case BinaryOperator.BitwiseOr => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 | i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 | i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 | i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 | i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.BitwiseOr(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Bitwise Xor.
              */
            case BinaryOperator.BitwiseXor => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 ^ i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 ^ i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 ^ i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 ^ i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.BitwiseXor(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Bitwise Left Shift.
              */
            case BinaryOperator.BitwiseLeftShift => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 << i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 << i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 << i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 << i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.BitwiseLeftShift(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }

            /**
              * Bitwise Right Shift.
              */
            case BinaryOperator.BitwiseRightShift => (v1, v2) match {
              // Concrete semantics.
              case (SymVal.Int8(i1), SymVal.Int8(i2)) => lift(pc, SymVal.Int8((i1 >> i2).toByte))
              case (SymVal.Int16(i1), SymVal.Int16(i2)) => lift(pc, SymVal.Int16((i1 >> i2).toShort))
              case (SymVal.Int32(i1), SymVal.Int32(i2)) => lift(pc, SymVal.Int32(i1 >> i2))
              case (SymVal.Int64(i1), SymVal.Int64(i2)) => lift(pc, SymVal.Int64(i1 >> i2))

              // Symbolic semantics.
              case _ =>
                val newVar = genSym.fresh2()
                val newPC = Expr.Equal(Expr.Var(newVar, exp0.tpe), Expr.BitwiseRightShift(toIntExpr(v1, exp1.tpe), toIntExpr(v2, exp2.tpe))) :: pc
                lift(newPC, SymVal.AtomicVar(newVar))
            }
          }
        }

      /**
        * If-then-else.
        */
      case Expression.IfThenElse(exp1, exp2, exp3, _, _) =>
        eval(pc0, exp1, env0) flatMap {
          case (pc, c) => c match {
            case SymVal.True => eval(pc, exp2, env0)
            case SymVal.False => eval(pc, exp3, env0)
            case SymVal.AtomicVar(id) =>
              // Evaluate both branches under different path constraints.
              val consequent = eval(Expr.Var(id, Type.Bool) :: pc, exp2, env0.clone())
              val alternative = eval(Expr.Not(Expr.Var(id, Type.Bool)) :: pc, exp3, env0.clone())
              consequent ++ alternative
            case v => throw InternalCompilerException(s"Type Error: Unexpected value: '$v'.")
          }
        }

      /**
        * Let-binding.
        */
      case Expression.Let(ident, _, exp1, exp2, _, _) =>
        eval(pc0, exp1, env0) flatMap {
          case (pc, v1) =>
            // Bind the variable to the value of `exp1` which is `v1`.
            val newEnv = env0.clone()
            newEnv += (ident.name -> v1)
            eval(pc, exp2, newEnv)
        }

      /**
        * Tags.
        */
      case Expression.Tag(enum, tag, exp, _, _) =>
        eval(pc0, exp, env0) flatMap {
          case (pc, v) => lift(pc, SymVal.Tag(tag.name, v))
        }

      /**
        * Tuples.
        */
      case Expression.Tuple(elms, _, _) =>
        evaln(pc0, elms, env0) flatMap {
          case (pc, es) => lift(pc, SymVal.Tuple(es))
        }

      /**
        * Check Tag Value.
        */
      case Expression.CheckTag(tag, exp, _) =>
        eval(pc0, exp, env0) flatMap {
          case (pc, SymVal.Tag(tag2, _)) =>
            if (tag.name == tag2)
              lift(pc, SymVal.True)
            else
              lift(pc, SymVal.False)
          case (_, v) => throw InternalCompilerException(s"Type Error: Unexpected value: '$v'.")
        }

      /**
        * Get Tag Value.
        */
      case Expression.GetTagValue(tag, exp, _, _) =>
        eval(pc0, exp, env0) flatMap {
          case (pc, SymVal.Tag(_, v)) => lift(pc, v)
          case v => throw InternalCompilerException(s"Type Error: Unexpected value: '$v'.")
        }

      /**
        * Get Tuple Index.
        */
      case Expression.GetTupleIndex(base, offset, _, _) =>
        eval(pc0, base, env0) flatMap {
          case (pc, SymVal.Tuple(elms)) => lift(pc, elms(offset))
          case v => throw InternalCompilerException(s"Type Error: Unexpected value: '$v'.")
        }

      /**
        * User Error.
        */
      case Expression.UserError(tpe, loc) => lift(pc0, SymVal.UserError(loc))

      /**
        * Match Error.
        */
      case Expression.MatchError(tpe, loc) => lift(pc0, SymVal.MatchError(loc))

      /**
        * Switch Error
        */
      case Expression.SwitchError(tpe, loc) => lift(pc0, SymVal.SwitchError(loc))

      // NB: Not yet fully implemented in the backend.
      case e: Expression.FSet => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.CheckNil => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.CheckCons => throw InternalCompilerException(s"Unsupported expression: '$e'.")

      /**
        * Unsupported expressions.
        */
      case e: Expression.Hook => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.Universal => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.Existential => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.LoadBool => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.LoadInt8 => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.LoadInt16 => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.LoadInt32 => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.StoreBool => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.StoreInt8 => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.StoreInt16 => throw InternalCompilerException(s"Unsupported expression: '$e'.")
      case e: Expression.StoreInt32 => throw InternalCompilerException(s"Unsupported expression: '$e'.")

    }

    /**
      * Returns a context with the value `v` guarded by the path constraint `pc`.
      */
    def lift(pc: PathConstraint, v: SymVal): List[(PathConstraint, SymVal)] = List(pc -> v)

    // TODO: DOC
    def eq(pc0: PathConstraint, x: SymVal, y: SymVal, tpe: Type): List[(PathConstraint, SymVal)] = (x, y) match {
      // TODO: Use type
      case (SymVal.AtomicVar(ident1), SymVal.AtomicVar(ident2)) =>
        // Two identifiers are either equal to each other or they are not.
        // We can encode this using two path constraints.
        List(
          (Expr.Equal(Expr.Var(ident1, tpe), Expr.Var(ident2, tpe)) :: pc0, SymVal.True),
          (Expr.NotEqual(Expr.Var(ident1, tpe), Expr.Var(ident2, tpe)) :: pc0, SymVal.False)
        )
      case (SymVal.Unit, SymVal.Unit) => lift(pc0, SymVal.True)
      case (SymVal.Tag(tag1, v1), SymVal.Tag(tag2, v2)) if tag1 == tag2 =>
        val innerTpe = tpe.asInstanceOf[Type.Enum]
        eq(pc0, v1, v2, innerTpe.cases(tag1))
      case (SymVal.Tag(tag1, v1), SymVal.Tag(tag2, v2)) if tag1 != tag2 =>
        lift(pc0, SymVal.False)

      // TODO: Rest
    }

    // TODO: DOC
    def eval2(pc0: PathConstraint, x: Expression, y: Expression, env0: Environment): List[(PathConstraint, (SymVal, SymVal))] =
      eval(pc0, x, env0) flatMap {
        case (pcx, vx) => eval(pcx, y, env0) map {
          case (pcy, vy) => pcy -> ((vx, vy))
        }
      }

    // TODO: DOC
    def evaln(pc0: PathConstraint, xs: Traversable[Expression], env0: Environment): List[(PathConstraint, List[SymVal])] = {
      /*
       * Local visitor.
       */
      def visit(pc: PathConstraint, xs: List[Expression], env: Environment): List[(PathConstraint, List[SymVal])] = xs match {
        case Nil => List((pc, Nil))
        case r :: rs => eval(pc, r, env) flatMap {
          case (pc1, v) => visit(pc1, rs, env) map {
            case (pc2, vs) => (pc2, v :: vs)
          }
        }
      }

      visit(pc0, xs.toList, env0)
    }

    /**
      * Converts the given value `v` to an expression.
      */
    def toIntExpr(v: SymVal, tpe: Type): Expr = (v, tpe) match {
      case (SymVal.AtomicVar(id), _) => Expr.Var(id, tpe)
      case (SymVal.Int8(i), Type.Int8) => Expr.Int8(i)
      case (SymVal.Int16(i), Type.Int16) => Expr.Int16(i)
      case (SymVal.Int32(i), Type.Int32) => Expr.Int32(i)
      case (SymVal.Int64(i), Type.Int64) => Expr.Int64(i)
      case _ => throw InternalCompilerException(s"Unexpected value: '$v' of type '$tpe'.")
    }

    //  TODO: Replace this by a different enumeration.
    def toSymVal(exp0: Expression): SymVal = exp0 match {
      case Expression.Unit => SymVal.Unit
      case Expression.Var(ident, _, _, _) => SymVal.AtomicVar(ident)
      case Expression.Tag(enum, tag, exp, tpe, loc) =>
        SymVal.Tag(tag.name, toSymVal(exp))
      case _ => ???
    }

    /**
      * Construct the initial environment.
      */
    val initEnv = mutable.Map.empty[String, SymVal]
    for ((name, exp) <- env0) {
      initEnv += (name -> toSymVal(exp))
    }

    eval(Nil, exp0, initEnv)
  }

  /**
    * Returns the symbolic value corresponding to the given boolean `b`.
    */
  private def toBool(b: Boolean): SymVal =
    if (b) SymVal.True else SymVal.False


  // TODO: Doc
  private def zeroOf(tpe: Type): Expr = tpe match {
    case Type.Int8 => Expr.Int8(0)
    case Type.Int16 => Expr.Int16(0)
    case Type.Int32 => Expr.Int32(0)
    case Type.Int64 => Expr.Int64(0)
    case _ => throw InternalCompilerException(s"Unexpected type '$tpe'.")
  }

  // TODO: DOC
  private def common(tpe1: Type, tpe2: Type): Type =
    if (tpe1 == tpe2)
      tpe1
    else
      throw InternalCompilerException(s"Unexpected non-equal types: '$tpe1' and '$tpe2'.")

  // TODO: DOC
  private def numeric(tpe: Type): Boolean = ???

}
