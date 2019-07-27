package com.example.none.pigmanbox.modle;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Game Class
 */
public class Game {
    private String gameName;
    private String packName;
    private File obbFile;
    private List<Mod> modList = new ArrayList<>();
    private List<Mod> planAddModlist = new ArrayList<>();
    private List<Mod> planDeleterModlist = new ArrayList<>();


    public Game() {
        this.gameName = "DefaultName";
        this.packName = "DefaulPackName";
        this.obbFile = new File("");
    }

    public Game(String gameName, String packName, File obbFile) {
        this.gameName = gameName;
        this.packName = packName;
        this.obbFile = obbFile;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public File getObbFile() {
        return obbFile;
    }

    public void setObbFile(File obbFile) {
        this.obbFile = obbFile;
    }

    public List<Mod> getModList() {
        return modList;
    }

    public void setModList(List<Mod> modList) {
        this.modList = modList;
    }

    public List<Mod> getPlanAddModlist() {
        return planAddModlist;
    }

    public void setPlanAddModlist(List<Mod> planAddModlist) {
        this.planAddModlist = planAddModlist;
    }

    public List<Mod> getPlanDeleterModlist() {
        return planDeleterModlist;
    }

    public void setPlanDeleterModlist(List<Mod> planDeleterModlist) {
        this.planDeleterModlist = planDeleterModlist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(gameName, game.gameName) &&
                Objects.equals(packName, game.packName) &&
                Objects.equals(obbFile, game.obbFile);
    }

    @Override
    public int hashCode() {

        return Objects.hash(gameName, packName, obbFile);
    }
}
