package com.get_slyncy.slyncy.Model.DTO;

/**
 * Created by undermark5 on 3/12/18.
 */

public class SlyncyImage
{
    private String name;
    private String content;

    public SlyncyImage(String name, String content)
    {
        this.name = name;
        this.content = content;
    }

    public String getName()
    {
        return name;
    }

    public String getContent()
    {
        return content;
    }
}
