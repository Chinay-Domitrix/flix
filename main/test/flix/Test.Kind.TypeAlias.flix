namespace Test/Enum/Kinding {
    namespace Implicit {
        type alias TStar[a] = a

        type alias EStarStar[a, b] = (a, b)
    }

    namespace Explicit {
        rel R(i: Int)

        type alias TStar[a: Type] = a

        type alias TStarStar[a: Type, b: Type] = (a, b)

        type alias TBool[a: Bool] = Int -> Int & a

        type alias TRecordRow[a: RecordRow] = { l :: Int | a }

        type alias TSchemaRow[a: SchemaRow] = #{ R | a }

        type alias TStarToStar[a: Type -> Type] = a[Int]

        type alias TBoolToStar[a: Bool -> Type] = a[Pure]

        type alias TRecordRowToStar[a: RecordRow -> Type, r: RecordRow] = a[r]

        type alias TSchemaRowToStar[a: SchemaRow -> Type, r: SchemaRow] = a[r]
    }

    @test
    def empty(): Bool = true
}