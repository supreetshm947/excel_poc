package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Country {
	@Id
	@Column(name="ID")
	private Double Id;
	@Column(name="NAME")
	private String Name;
	@Column(name="CURRENCY")
	private String Currency;
	public Double getId() {
		return Id;
	}
	public void setId(Double id) {
		Id = id;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getCurrency() {
		return Currency;
	}
	public void setCurrency(String currency) {
		Currency = currency;
	}
	
	
}
