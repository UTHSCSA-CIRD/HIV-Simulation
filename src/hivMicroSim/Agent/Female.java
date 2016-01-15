/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.AlloImmunity;
import hivMicroSim.HIV.DiseaseMatrix;
import hivMicroSim.HIV.Genotype;
import hivMicroSim.HIV.HIVInfection;
import hivMicroSim.HIVMicroSim;
import hivMicroSim.Infection;
import hivMicroSim.Relationship;
import hivMicroSim.SeroImmunity;
import java.util.ArrayList;
import sim.portrayal.*;
import sim.engine.*;

import java.awt.*;
/**
 *
 * @author ManuelLS
 */
public class Female extends Agent implements Steppable{
    private static final long serialVersionUID = 1;
    
    public Female(int id, int faithfullness, int condomUse, double wantLevel, double lack, boolean ccr51, boolean ccr52, double immuneFactors, 
            int age){
        super(id, faithfullness, condomUse, wantLevel, lack, ccr51, ccr52, immuneFactors, true, age);
        
    }
    public Female(int id, int faithfullness, int condomUse, double wantLevel, double lack, boolean ccr51, boolean ccr52, double immuneFactors, 
            ArrayList<SeroImmunity> sero, ArrayList<AlloImmunity> allo, int age, ArrayList<Infection> coinfections ){
        super(id, faithfullness, condomUse, wantLevel, lack, ccr51, ccr52, immuneFactors, true, sero, allo, age, coinfections);
    }
    public Female(int id, int faithfullness, int condomUse, double wantLevel, double lack, boolean ccr51, boolean ccr52, double immuneFactors, 
            ArrayList<SeroImmunity> sero, ArrayList<AlloImmunity> allo, int age, ArrayList<Infection> coinfections, DiseaseMatrix disease){
        super(id,faithfullness, condomUse, wantLevel, lack, ccr51, ccr52, immuneFactors, true, sero, allo, age, coinfections, disease);
    }
    @Override
    public boolean isFemale(){return true;}
    
    @Override
    public boolean addEdge(Relationship a){
        
        if(a.getFemale()!= this){
            return false; //invalid edge! 
        }
        //make sure we're not duplicating a relationship
        int o = a.getMale().ID;
        if (!network.stream().noneMatch((f) -> (f.getMale().ID == o))) return false;
        
        network.add(a);
        
        //System.out.print("DEBUG: Add Relationship. Network Level: " + networkLevel + " added: " + a.getCoitalFrequency());
        networkLevel += a.getCoitalFrequency();
        //System.out.print(" new Network Level: " + networkLevel + "\n");
        adjustLack(-a.getCoitalFrequency());
        return true;
    }
    @Override
    public boolean removeEdge(Relationship a){
        
        int o = a.getMale().ID;
        for(int i = 0; i<network.size();i++){
            if(network.get(i).getMale().ID == o){
                //System.out.print("DEBUG: Delete Relationship: " + networkLevel + " removed " + network.get(i).getCoitalFrequency());
                networkLevel -= network.get(i).getCoitalFrequency();
                network.remove(i);
                //System.out.print(" new Network Level: " + networkLevel + "\n");
                return true;
            }
        }
        
        return false;// could not find the edge! 
    }
   
    @Override
    public void step(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        age++;
        //adjust disease
        if(infected){
            if(hiv.progress(1)){
                //we have progressed in the infection. 
                int stage = hiv.getStage();
                //temporary debug
                switch(stage){
                    case 1:
                        col = Color.red;
                        break;
                    case 2:
                        col = Color.GREEN;
                        break;
                    case 3: 
                        col = Color.orange;
                        break;
                    case 4: 
                        col = Color.black;
                        //remove all relationships.
                        Relationship r;
                        int ns = network.size();
                        ns--;
                        for(int i = ns; i>= 0; i--){//start with the last element and work down to empty out the list
                            r = network.get(i);
                            r.getMale().removeEdge(r);
                            sim.network.removeEdge(r);
                        }
                        network.clear();
                        alive = false;
                        stopper.stop();
                }
                System.out.println("DEBUG: Agent changed to stage " + stage);
            }
        }
        //adjust for network edges (note, this does not change the edges, just adds their effect and potential infection. 
        double adj = wantLevel;
        Agent other;
        forEachRelationship:
        for (Relationship network1 : network) {
            adj -= network1.getCoitalFrequency();
            other = network1.getMale();
            if(other.infected){
////////////////////////// TODO -- handle condom use! Least likely to use condoms with spouse, most likely to use them with one shot.            
                
                //select a genotype from the other 
                ArrayList<HIVInfection> otherInfections = other.getDiseaseMatrix().getGenotypes(); 
                HIVInfection infection;
                //if the other has more than one genotype, select one, otherwise use that one. 
                if(otherInfections.size() >1){
                    //set mean of 0 with max range of list size. This makes you most likely to select an item closer to 0 or with larger virulence. 
                    int roll = Math.abs(sim.getGaussianRange(-(otherInfections.size()-1), (otherInfections.size()-1)));
                    infection = otherInfections.get(roll);
                }else{
                    infection = otherInfections.get(0);
                }
                if(infected){
                    for (HIVInfection mine : hiv.getGenotypes()) {
                        if(mine.getGenotype() == infection.getGenotype())continue forEachRelationship;
                    }
                }
                //attempt infection
///////////////////Calculated frequency of unprotected coitus. 
                if(attemptCoitalInfection(sim, infection, other.getDiseaseMatrix().getStage(), network1.getCoitalFrequency(), other.ID, 1.0)){
                    //We've been infected!
                    System.out.println("DEBUG: INFECTED!!!!");
                    infect(sim.genotypeList.get(infection.getGenotype()));
                }
                
            }
        }
        //System.out.print("DEBUG: Lack: " + lack + " want: " + wantLevel + " Network Level: " + networkLevel + " of size " + network.size() + " produced: " );
        adjustLack(adj);
        //System.out.print(" new lack: " + lack + "\n");
    }
    public boolean attemptCoitalInfection(HIVMicroSim sim, HIVInfection infection, int stage, int frequency, int agent, double degree){
        //this calculates the potential reduction from alloimmunity, then passes it on to attemptInfection. 
        int alloImmunity = getAlloImmunity(agent);
        addAlloImmunity(agent, frequency);
        if(alloImmunity > 100){
            degree= 1/(alloImmunity *.01);
        }
        //Since we're in the female class, we'll add the "female factor" to the degree since the risk of infection is higher
        //for the female in heterosexual coitus. -- later additional factors may be added for homosexual coitus for males. 
        degree = degree*sim.femaleLikelinessFactor;
////////////////////////This needs refining.
        
        for(int i = 0; i< frequency; i++){
            if(attemptInfection(sim, infection, stage, degree)) return true;
        }
        return false;
    }
    
    
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(col);
        int x = (int)(info.draw.x - (info.draw.width+1) / 2.0);
        int y = (int)(info.draw.y - (info.draw.height+1) / 2.0);
        int width = (int)(info.draw.width) +1;
        int height = (int)(info.draw.height) +1;
        
        graphics.fillOval(x,y,width, height);
    }
}
