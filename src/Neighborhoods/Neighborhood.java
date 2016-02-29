/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Neighborhoods;
/**
 *
 * @author ManuelLS
 */
public class Neighborhood{
    public static final long serialVersionUID = 1;
    public final String name;
    public final int ID;
    public final int type;
    public static final int TYPE_RACE = 1;
    public static final int TYPE_RELIGION = 2;
    public static final int TYPE_OTHER = 3;
    
    //influences- These are the rates prefered by the neighborhood, the agent will have an 
    //average of all neighborhood and their own innate level.
    //a -1 represents NO influence. E.g. a religion will not influence the want level of an agent, but 
    //being a member of a swinger's group would. 
    public final double condomUsage; // between 0 and 1 inclusive
    public final int faithfulness;
    public final int want;
    //additional behavioral modifications
    /*Selectiveness -> this is how much the members of this group are likely to select
    * ONLY members of their own group to copulate with. 0 means non-selective 10 means refuses 
    * to select from outside group.
    */
    public final int selectiveness; 
    
    public Neighborhood(int neighborhoodID, String neighborhoodName, int neighborhoodType, double condom, int faithfulness, int want, int selective){
        ID = neighborhoodID;
        name = neighborhoodName;
        type = neighborhoodType; 
        condomUsage = condom;
        this.faithfulness = faithfulness;
        this.want = want;
        selectiveness = selective;
    }
    
}
