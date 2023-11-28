package wethinkcode.places.db.memory;

import wethinkcode.places.model.Places;
import wethinkcode.places.model.Town;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class PlacesDb implements Places {
    private final Set<Town> townSet = new TreeSet<>();

    public PlacesDb(Set<Town> places) {
        townSet.addAll(places);
    }

    @Override
    public Collection<String> provinces() {
        return townSet.parallelStream()
                .map(Town::getProvince)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<Town> townsIn(String aProvince) {
        return townSet.parallelStream()
                .filter(town -> town.getProvince().equals(aProvince))
                .collect(Collectors.toSet());
    }

    @Override
    public int size() {
        return townSet.size();
    }
}
