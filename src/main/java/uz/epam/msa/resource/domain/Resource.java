package uz.epam.msa.resource.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "resource")
@Data
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String path;
    private String name;
    private String contentType;
    private Long size;
    private boolean deleted;

}
