package com.printhelloworld.hobby.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.redis.core.RedisHash;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Hobby.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@RedisHash("Hobby")
@Entity(name = "Hobby")
@Table(name = "hobbies", uniqueConstraints = {
        @UniqueConstraint(name = "unique_hobby_id", columnNames = "id"),
        @UniqueConstraint(name = "unique_hobby_name", columnNames = "name")
})
public class Hobby implements Serializable {
    @Id
    @SequenceGenerator(name = "hobby_sequence", sequenceName = "hobby_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hobby_sequence")
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon_link")
    private String iconLink;

    @ManyToMany
    @JoinTable(name = "hobby_categories", joinColumns = @JoinColumn(name = "hobby_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories = new ArrayList<>();

    public Hobby(Long id, String name, String iconLink, List<Category> categories) {
        this.id = id;
        this.name = name;
        this.iconLink = iconLink;
        this.categories = categories;
    }

    public Hobby() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconLink() {
        return iconLink;
    }

    public void setIconLink(String iconLink) {
        this.iconLink = iconLink;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Hobby hobby = (Hobby) o;
        return Objects.equals(id, hobby.id) && Objects.equals(name, hobby.name)
                && Objects.equals(iconLink, hobby.iconLink) && Objects.equals(categories, hobby.categories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, iconLink, categories);
    }
}
