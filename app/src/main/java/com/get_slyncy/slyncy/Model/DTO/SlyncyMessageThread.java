package com.get_slyncy.slyncy.Model.DTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tylerbowers on 2/26/18.
 */

public class SlyncyMessageThread
{

    private int threadId;
    private int messageCount;
    private List<String> numbers;
    private List<Contact> contacts;
    private List<SlyncyMessage> messages;
    private int imageCount;
    private boolean isRead;

    public SlyncyMessageThread()
    {
        contacts = Collections.synchronizedList(new ArrayList<Contact>());
        messages = Collections.synchronizedList(new ArrayList<SlyncyMessage>());
        imageCount = 0;
    }

    public int getThreadId()
    {
        return threadId;
    }

    public void setThreadId(int threadId)
    {
        this.threadId = threadId;
    }

    public int getMessageCount()
    {
        return messages.size();
    }

    public List<String> getNumbers()
    {
        return numbers;
    }

    public void setNumbers(List<String> numbers)
    {
        this.numbers = numbers == null ? null : Collections.synchronizedList(numbers);
    }

    public boolean addNumber(String number)
    {
        return numbers.add(number);
    }

    public List<Contact> getContacts()
    {
        return contacts;
    }

    public void setContacts(List<Contact> contacts)
    {
        this.contacts = contacts;
    }

    public boolean addContact(Contact contact)
    {
        for (Contact contact1 : contacts)
        {
            if (contact1.equals(contact))
            {
                return false;
            }
        }
        for (SlyncyMessage message : messages)
        {
            message.addContact(contact);
        }

        return contacts.add(contact);
    }

    public List<SlyncyMessage> getMessages()
    {
        return messages;
    }

    public void setMessages(List<SlyncyMessage> messages)
    {
        this.messages = messages;
    }

    public boolean addMessage(SlyncyMessage message)
    {
        messageCount++;
        return messages.add(message);
    }

    public int getImageCount()
    {
        return imageCount;
    }

    public void incrementImageCount()
    {
        imageCount++;
    }

    public boolean isRead()
    {
        return isRead;
    }

    public void setRead(boolean read)
    {
        isRead = read;
    }
}
