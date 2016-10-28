package cn.zdxiang.jmeasyrecyclerviewsimple;

/**
 * Created on 16/10/28.
 *
 * @author JM
 * @version v1.0
 * @discrition ContentEntity
 */

public class ContentEntity {
    public ContentEntity(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
