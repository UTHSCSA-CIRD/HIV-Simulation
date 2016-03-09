/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Neighborhoods;

import hivMicroSim.Agent.Agent;
import hivMicroSim.HIVMicroSim;

/**
 *
 * @author ManuelLS
 */
public abstract class NeighborhoodTemplates {
    /*
    int neighborhoodID, String neighborhoodName, int neighborhoodType, int condom, int faithfulness, int want, int selective
    -- for the record--- the initial version of these numbers are all fudged... I have no idea. 
    */
    public static final int Race_Caucasian = 1;
    public static final int Race_Hispanic = 2;
    public static final int Race_Black = 3;
    public static final int Race_Other = 4;
    
    //religions
    public static final int Religion_Catholic = 1; 
    
    //percents for assigning -- these must be < 100 - for race they must == 100
    //RACE PERCENTS
    public static final int percentCaucassian = 50;
    public static final int percentHispanic = 30;
    public static final int percentOther = 15;
    public static final int percentBlack = 5;
    //RELGION PERCENTS
    public static final int percentCatholic = 20;
    public static final int percentBuddist = 5;
    
    //other
    public static final int percentSwinger = 2;
    
    //These are not currently in neighborhoods, but this is a good place to store
    //them
    //sexuality (note that this might later have to be parsed by male vs female
    public static final int percentHomosexual = 5;
    public static final int percentBiSexual = 10;
    
    public static byte getSexuality(HIVMicroSim sim){
        int roll = sim.random.nextInt(100);
        if(roll < percentBiSexual) return Agent.ORIENTATION_BISEXUAL;
        int running = percentBiSexual + percentHomosexual;
        if(roll < running) return Agent.ORIENTATION_HOMOSEXUAL;
        return Agent.ORIENTATION_HETEROSEXUAL;
    }
    
    public static Neighborhood getRace(HIVMicroSim sim){
        int roll = sim.random.nextInt(100);
        if(roll < percentCaucassian) return sim.race_Caucassian;
        int running = percentCaucassian + percentHispanic;
        if(roll < running) return sim.race_Hispanic;
        running += percentOther;
        if(roll <running) return sim.race_Black;
        return sim.race_Other;
    }
    public static Neighborhood getReligion(HIVMicroSim sim){
        int roll = sim.random.nextInt(100);
        if(roll < percentCatholic) return sim.religion_Catholic;
        int running = percentCatholic+percentBuddist;
        if(roll < running) return sim.religion_Buddist;
        return null;
    }
    public static Neighborhood getOther(HIVMicroSim sim){
        //to be filled in when we later add "others"
        int roll = sim.random.nextInt(100);
        if(roll < percentSwinger) return sim.other_Swinger;
        return null;
    }
    
    ////////******************** RACE TEMPLATES ******************/////////////////////////////
    public static Neighborhood getRaceCaucasian(){
        /*
        ID - 1
        Name = "Caucasian"
        Type = TYPE_RACE
        condom = .8
        faithfulness = 10
        want = -1
        selective = 5
        */
        return new Neighborhood(1, "Caucasian", Neighborhood.TYPE_RACE, .8, 10, -1, 5);
    }
    public static Neighborhood getRaceHispanic(){
        /*
        ID - 2
        Name = "Hispanic"
        Type = TYPE_RACE
        condom = -1
        faithfulness = -1
        want = 20
        selective = 3
        */
        return new Neighborhood(2, "Hispanic", Neighborhood.TYPE_RACE, -1, -1, 20, 3);
    }
    public static Neighborhood getRaceBlack(){
        /*
        ID - 3
        Name = "Black"
        Type = TYPE_RACE
        condom = 0
        faithfulness = -1
        want = -1
        selective = 5
        */
        return new Neighborhood(3, "Black", Neighborhood.TYPE_RACE, 0, -1, -1, 5);
    }
    public static Neighborhood getRaceOther(){
        /*
        ID - 4
        Name = "Other"
        Type = TYPE_RACE
        condom = -1
        faithfulness = -1
        want = -1
        selective = 0
        */
        return new Neighborhood(4, "Other", Neighborhood.TYPE_RACE, -1, -1, -1, 0);
    }
    
    ////////////////////////************************ RELIGION TEMPLATES *****************//////////////////
    public static Neighborhood getReligionCatholic(){
        /*
        ID - 1
        Name = "Catholic"
        Type = TYPE_RELIGION
        condom = 0
        faithfulness = 30
        want = 30
        selective = 8
        */
        return new Neighborhood(1, "Catholic", Neighborhood.TYPE_RELIGION, 0, 30, 30, 8);
    }
    public static Neighborhood getReligionBuddist(){
        /*
        ID - 2
        Name = "Buddist"
        Type = TYPE_RELIGION
        condom = -1
        faithfulness = 20
        want = -1
        selective = 3
        */
        return new Neighborhood(2, "Buddist", Neighborhood.TYPE_RELIGION, -1, 20, -1, 3);
    }
    public static Neighborhood getOtherSwinger(){
        /*
        ID - 1
        Name = "Swinger"
        Type = TYPE_Other
        condom = 1
        faithfulness = 0
        want = 30
        selective = 8
        */
        return new Neighborhood(1, "Swinger", Neighborhood.TYPE_OTHER, -1, 0, 30, 8);
    }
    
}
