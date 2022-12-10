package dto;

public class Ingredient {
    private final String name;
    private final String measure_unit_name;
    private final int count;

    public Ingredient(String name, String measure_unit_name, int count){
        this.name = name;
        this.measure_unit_name = measure_unit_name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getMeasure_unit_name() {
        return measure_unit_name;
    }

    public int getCount() {
        return count;
    }

    public String getInfo(){
        return getName() + ": " + getCount() + " " + getMeasure_unit_name() + "\n";
    }
}
