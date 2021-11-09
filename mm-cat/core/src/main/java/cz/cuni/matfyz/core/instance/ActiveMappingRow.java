package cz.cuni.matfyz.core.instance;

/**
 * This class represents a relation between two members of two active domains ({@link ActiveDomainRow}).
 * It corresponds to a single {@link InstanceMorphism}.
 * @author jachym.bartik
 */
public class ActiveMappingRow
{
	private final ActiveDomainRow domainRow;
    private final ActiveDomainRow codomainRow;
    
    public ActiveDomainRow domainRow()
    {
        return domainRow;
    }
    
    public ActiveDomainRow codomainRow()
    {
        return codomainRow;
    }
    
    public ActiveMappingRow(ActiveDomainRow domainRow, ActiveDomainRow codomainRow)
    {
        this.domainRow = domainRow;
        this.codomainRow = codomainRow;
    }
}