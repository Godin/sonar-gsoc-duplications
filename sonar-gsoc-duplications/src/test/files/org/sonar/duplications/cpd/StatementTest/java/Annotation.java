@Entity
@Table(name = "properties")
public class Property extends BaseIdentifiable {

  @Column(name = "prop_key", updatable = true, nullable = true)
  private String key;

  @Column(name = "text_value", updatable = true,
      nullable = true, length = 167772150)
  @Lob
  private char[] value;

  @Override
  public Integer getUserId() {
    return userId;
  }
}