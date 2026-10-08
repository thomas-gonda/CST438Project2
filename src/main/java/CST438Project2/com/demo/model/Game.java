package CST438Project2.com.demo.model;

import java.math.BigDecimal;

public class Game {

    private Long id;
    private String name;
    private String publisher;
    private String category;
    private BigDecimal price;

    public Game() {
    }

    public Game(
            Long id,
            String name,
            String publisher,
            String category,
            BigDecimal price
    ) {
        this.id = id;
        this.name = name;
        this.publisher = publisher;
        this.category = category;
        this.price = price;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", publisher='" + publisher + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                '}';
    }
}
