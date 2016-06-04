package com.dhorby.semantics.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;


@XmlRootElement(name="datavalue")
public class DataValue implements Serializable {

    @XmlElement(name="category")
    String category;

    @XmlElement(name="key")
    String key;

    @XmlElement(name="value")
    String value = "";

    public DataValue(String category, String key, String value) {
        this.category=category;
        this.key=key;
        this.value=value;
    }

    public DataValue() {
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

	/*public String toString() {
		return "Key:" + key + " Category:" + Journals.getInstance().getJournalTitle(category) + " Value:" + value;
	}

	public String toTSV() {
		return  Journals.getInstance().getJournalTitle(category) + "	" + key + "	" + value ;
	}*/


}

