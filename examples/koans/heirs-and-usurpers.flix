pub def heirsAndUsurpers(parents: Array[(person, person)], emperors: Array[person]): {heirs :: Array[person], usurpers :: Array[person]} with Boxable[person] =
    let p = project parents into Parent;
    let e = project emperors into Emperor;
    let lp = #{
        Ancestor(x, y) :- Parent(x, y), not Emperor(y).
        Ancestor(x, z) :- Ancestor(x, y), Parent(y, z), not Emperor(z).
        HeirOf(x, y) :- Emperor(x), Parent(x, y), Emperor(y).
        HeirOf(x, z) :- Emperor(x), Ancestor(x, y), Parent(y, z), Emperor(z).
        Heir(x) :- HeirOf(x, _).
        Usurper(x) :- Emperor(x), not Heir(x).
    };
    let solution = solve p, e, lp;
    let heirs = query solution select x from Heir(x);
    let usurpers = query solution select x from Usurper(x);
    {heirs = heirs, usurpers = usurpers}
