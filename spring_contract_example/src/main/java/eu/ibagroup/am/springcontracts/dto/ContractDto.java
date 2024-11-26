package eu.ibagroup.am.springcontracts.dto;

public class ContractDto {
    
    private int id;

    private String contractNumber;

    private String country;

    public ContractDto() {
	super();
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContractNumber() {
	return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
	this.contractNumber = contractNumber;
    }

    public String getCountry() {
	return country;
    }

    public void setCountry(String country) {
	this.country = country;
    }

}
