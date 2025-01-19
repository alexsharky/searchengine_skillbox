package searchengine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@NoArgsConstructor
@Entity
@Table(name = "indexes")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false)
    @NonNull
    private Page page;

    @ManyToOne(cascade = CascadeType.MERGE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(nullable = false)
    @NonNull
    private Lemma lemma;

    @Column(name = "rank_count", columnDefinition = "FLOAT", nullable = false)
    private float rank;
}
