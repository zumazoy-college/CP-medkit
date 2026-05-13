package com.medkit.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "genders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gender {

    @Id
    @Column(name = "id_gender")
    private Integer idGender;

    @Column(nullable = false, unique = true, length = 10)
    private String name;
}
