pub def drivable(roads: Array[(city, Int, city)], source: city, destination: city, minimumSpeed: Int): Bool with Boxable[city] =
    let r = project roads into Road;
    let lp = #{
        Path(x, y) :- Road(x, maximumSpeed, y), if maximumSpeed > minimumSpeed.
        Path(x, z) :- Path(x, y), Road(y, maximumSpeed, z), if maximumSpeed > minimumSpeed.
    };
    not Array.isEmpty(query r, lp select () from Path(source, destination))
