package com.example.none.pigmanbox.modle;

import com.example.none.pigmanbox.util.ModUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mod Class
 */
public class Mod{
    private int id;
    private boolean exist;
    private String name;
    private String author;
    private String description;
    private List<String> tags = new ArrayList<>();

    public Mod(){}

    public Mod(int id, boolean exist, String name, String author, String description) {
        this.id = id;
        this.exist = exist;
        this.name = name;
        this.author = author;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mod mod = (Mod) o;
        return id == mod.id &&
                Objects.equals(name, mod.name) &&
                Objects.equals(author, mod.author) &&
                Objects.equals(description, mod.description) &&
                Objects.equals(tags, mod.tags);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, author, description, tags);
    }

    @Override
    public String toString() {
        return "Mod{" +
                "id=" + id +
                ", exist=" + exist +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", tags=" + tags +
                '}';
    }
}
