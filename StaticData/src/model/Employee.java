package model;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.type.TrueFalseType;

@Entity
public class Employee {
	
	@javax.persistence.Id
	@Column(name="ID")
	private Double Id;
	
	
	@Column(name="NAME",nullable=false,unique=true)
	private String name;
	
	@Column(name="COMP_ID",nullable=false,unique=true)
	private Double comp_id;
	
	@Column(name="COMPANY")
	private String Company;

	public Double getId() {
		return Id;
	}

	public void setId(Double id) {
		Id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getComp_id() {
		return comp_id;
	}

	public void setComp_id(Double comp_id) {
		this.comp_id = comp_id;
	}

	public String getCompany() {
		return Company;
	}

	public void setCompany(String company) {
		Company = company;
	}
	
}
