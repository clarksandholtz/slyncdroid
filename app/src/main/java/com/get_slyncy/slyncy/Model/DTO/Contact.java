package com.get_slyncy.slyncy.Model.DTO;

/**
 * Created by tylerbowers on 2/12/18.
 */

public class Contact
{

    private static final String TAG = "Contact";

    private String phone;
    private String name;

    public Contact(String number)
    {
        this.phone = number;
        if (number == null || number.length() < 1)
        {
            return;
        }
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj.getClass() != this.getClass())
        {
            return false;
        }

        Contact c = (Contact) obj;

        if (!this.getName().equalsIgnoreCase(c.getName()))
        {
            return false;
        }

        return true;
    }
}
