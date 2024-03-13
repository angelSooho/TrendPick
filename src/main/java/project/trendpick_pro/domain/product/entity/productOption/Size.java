package project.trendpick_pro.domain.product.entity.productOption;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Size {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    public Size(String name) {
        this.name = name;
    }
}
