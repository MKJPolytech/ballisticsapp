package kr.ac.kopo.ballisticsapp;

public class Bullet {
    private String name;        // 탄 이름 (예: M193 FMJ)
    private double massGram;    // 질량 (gram)
    private double velocityMps; // 초기 속도 (m/s)
    private String category;    // Rifle / Shotgun / Handgun

    public Bullet(String name, double massGram, double velocityMps, String category) {
        this.name = name;
        this.massGram = massGram;
        this.velocityMps = velocityMps;
        this.category = category;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getMassGram() {
        return massGram;
    }

    public double getVelocityMps() {
        return velocityMps;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return name + " (" + massGram + "g, " + velocityMps + "m/s)";
    }

    public boolean isCustom() {
        return name.contains("CUSTOM");
    }

}

