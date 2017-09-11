library(ggplot2)
library(reshape2)
library(car)#recode
library(sqldf)

##Network libraries Version 1
library(igraph)

##
agentLog = read.table("agentLog.txt", header = TRUE, sep = "\t")
eventLog = read.table("eventLog.txt", header = TRUE, sep = "\t")
yearLog = read.table("yearLog.txt", header = TRUE, sep = "\t")
yearLog$percentInfected = yearLog$Prevelance/yearLog$Starting.Population * 100
yearLog$incidenceRate = yearLog$Incidence_Total/yearLog$Starting.Population * 1000
yearLog$incidenceRate_MSM = yearLog$Incidence_MSM/yearLog$Starting.Population * 1000
yearLog$incidenceRate_MSWO = yearLog$Incidence_MSWO/yearLog$Starting.Population * 1000
yearLog$incidenceRate_Female = yearLog$Incidence_Female/yearLog$Starting.Population * 1000

vaginalInfect = eventLog[grep("Vaginal", eventLog$Action),]
analInfect = eventLog[grep("Anal", eventLog$Action),]
infect = rbind(vaginalInfect, analInfect)
rNot = table(infect$Agent)

## TODO--- infAgents does not contain the initially infected agents. This needs to be accounted for later on!!

highR0 = as.data.frame(rNot[rNot>2])
iAgents = agentLog[agentLog$ID%in%highR0$Var1,]
#changing this to grep for 'sexual' to avoid mother to child infections.
infAgents = agentLog[agentLog$ID %in% infect[,"Desc1_AgeAgent"],]
nonInfAgents = agentLog[!agentLog$ID %in% infect[,"Desc1_AgeAgent"],]
popGrowthRate = log(yearLog[nrow(yearLog), "Starting.Population"]/yearLog[1,"Starting.Population"])/nrow(yearLog)
infectPattern = sqldf(
  'select inf.*, e.infector, ePA.toAIDs, ePD.toDeath, disc.toDiscovery, rNot.infected, duration.Infection_Duration
  from infAgents inf
  left join (select "Desc1.StageAgeAgent." infected, Agent infector from eventLog where Action like "Vaginal%" or Action like "Anal%") e on inf.ID == e.infected
  left join (select Agent, Desc2_Ticks toAIDs from eventLog where Action == "Progression" and Desc1_AgeAgent == 3) ePA on inf.ID == ePA.Agent
  left join (select Agent, Desc1_AgeAgent toDeath from eventLog where Action == "AIDS Death") ePD on inf.ID == ePD.Agent
  left join (select Agent, Desc2_Ticks toDiscovery from eventLog where Action == "Discovery") disc on inf.ID == disc.Agent
  left join (select Agent, count(*) infected from infect group by Agent) rNot on inf.ID == rNot.Agent 
  left join (select e.Agent Agent, (e.Tick - ei.Tick) Infection_Duration 
  from eventLog e 
  join (select Tick, "Desc1_AgeAgent" Agent 
  from eventLog
  where Action in ("Vaginal Insertive","Vaginal Receptive","Anal Insertive","Anal Receptive")
  ) ei on e.Agent = ei.Agent
  where e.Action in ("AIDS Death", "Infected Non-AIDS Death")) duration on duration.Agent = inf.ID
  ')
#For some reason the number of people infected is showing up as a character sometimes. 
infectPattern$infected = as.integer(infectPattern$infected)
infectPattern$Infection_Duration = as.numeric(infectPattern$Infection_Duration)
infectPattern$infected[is.na(infectPattern$infected)] = 0
infectPattern$InfPerYear = infectPattern$infected/(infectPattern$Infection_Duration/52)

## Networks code 

#create links (d)
infect$Desc7_ClusterGroup = as.factor(infect$Desc7_ClusterGroup)
links = infect[,c(2,4, 3,7, 8, 9, 10)]



 # Convert the text or numeric field 2 to numeric
links[,2] = as.numeric(links[,2])
links$Action = factor(links$Action, levels = c('Anal Insertive', 'Anal Receptive', 'Vaginal Insertive','Vaginal Receptive'))
links$Desc4_Stage = factor(links$Desc4_Stage, levels = c('1','2','3'))
stageCol = c('red','green','gold')
links$Desc5_KnownStatus = factor(links$Desc5_KnownStatus, levels = c('false', 'true'))
links$Desc6_TreatmentStatus = factor(links$Desc6_TreatmentStatus, levels = c('false', 'true'))

paste("Cluster Size: Min:", min(table(links$Desc7_ClusterGroup)), "Mean:", mean(table(links$Desc7_ClusterGroup)),
      "Max:", max(table(links$Desc7_ClusterGroup)))
quantile(table(links$Desc7_ClusterGroup))
hist(table(links$Desc7_ClusterGroup))
ggplot(links, aes(Desc7_ClusterGroup, fill = Action)) + geom_bar(position="stack")
ggplot(links, aes(Desc7_ClusterGroup, fill = Desc5_KnownStatus)) + geom_bar(position="stack")
ggplot(links, aes(Desc7_ClusterGroup, fill = Desc6_TreatmentStatus)) + geom_bar(position="stack")

# Create/identify nodes Dataframe: infAgents
nodes = sqldf("select ID, case 
                    when Gender ='M' and MSM = 'true' and MSW = 'true' then 'MSMW' 
                    when Gender ='M' and MSM = 'true' and MSW = 'false' then 'MSMO'
                    when Gender ='M' and MSM = 'false' and MSW = 'true' then 'MSWO'
                    else 'F' end Type,
                  Gender, MSW, MSM
              from agentLog 
              where id in (select Agent from links) or id in (select Desc1_AgeAgent from links)")# 

nodes$Type = factor(nodes$Type, levels = c('F', 'MSMO', 'MSMW','MSWO'))
typeCol = c('pink', 'skyblue', 'green', 'orange')
##Use this to narrow down to specific clusters for display.
###Create subnetworks of 
plotCluster <- function(links, nodes){
  tab = table(links$Desc7_ClusterGroup)
  repeat{
    print("Available clusters and their size:")
    print(tab)
    clusterID <- readline(prompt ="Which cluster should we plot? ")
    if(as.integer(clusterID) %in% links$Desc7_ClusterGroup){
      break
    }
    print("ERROR: Not a valid choice. Enter the integer clusterID.")
    Sys.sleep(2) #pause so the user can read the error... 
  }
  #plotting selected 
  plotLink <- links[links$Desc7_ClusterGroup == clusterID, ]
  plotNodes <- nodes[nodes$ID %in% plotLink$Agent | nodes$ID %in% plotLink$Desc1_AgeAgent,]
  net <- graph_from_data_frame(d=plotLink, vertices = plotNodes, directed = T)
  plot(net, vertex.color = typeCol[plotNodes$Type], edge.arrow.size = .5, edge.color = stageCol[plotLink$Desc4_Stage], vertex.label = NA,
       vertex.size = 5, margin = c(0,0,0,0), main = paste("Cluster", clusterID, "Network Graph"))
  legend(x = 1, y = -1, legend = c("Females", "MSMO", "MSMW", "MSWO"),pch=21, pt.bg= typeCol, pt.cex = 2, cex=.8,bty="n", ncol = 1)
}

#TODO: Add MSM MSWO and W to the year log.

plot(yearLog$Year, yearLog$percentInfected, type = "l", main = "Prevalence in Agent Population",
     ylab = "Percent Infected", xlab = "Model Year")
plot(yearLog$Starting.Population, type = "l", main = "Model Population")

#Multivariable plots
tmp = yearLog[, c("Year","Starting.Population","Prevelance")];melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line() 
tmp = yearLog[, c("Year","percentInfected","incidenceRate")];tmp$mortalityRate = (yearLog$Mortality/yearLog$Starting.Population * 1000);melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line() + ggtitle("Prevalence, Incidence, and Mortality")

#Rnot and t-test- Compare population profiles for non-infected, infected, and high Rnot

paste("Mean rNot (in those that did infect others):", mean(rNot))
paste("Max rNot:", max(rNot))

summary(agentLog)
summary(nonInfAgents)
summary(infAgents)
summary(iAgents)

#all infected vs non infected agents 
t.test(infAgents$Commitment, nonInfAgents$Commitment)
t.test(infAgents$Monogamous, nonInfAgents$Monogamous)
t.test(infAgents$Libido, nonInfAgents$Libido)
t.test(infAgents$Condom.Usage, nonInfAgents$Condom.Usage)
t.test(infAgents$Immunity, nonInfAgents$Immunity)


#high infectors And Non Infected
t.test(iAgents$Commitment, nonInfAgents$Commitment)
t.test(iAgents$Monogamous, nonInfAgents$Monogamous)
t.test(iAgents$Libido, nonInfAgents$Libido)
t.test(iAgents$Condom.Usage, nonInfAgents$Condom.Usage)
t.test(iAgents$Immunity, nonInfAgents$Immunity)

#t.test(agentLog$Selectivity, iAgents$Selectivity)
#infected agents (minus initial infected who were randomly selected) and high infectors
t.test(iAgents$Commitment, infAgents$Commitment)
t.test(iAgents$Monogamous, infAgents$Monogamous)
t.test(iAgents$Libido, infAgents$Libido)
t.test(iAgents$Condom.Usage, infAgents$Condom.Usage)
t.test(iAgents$Immunity, infAgents$Immunity)

########Looking at the duration of infection#####
#build the infection data set

if(length(which(!is.na(infectPattern$toAIDs))) != 0){ print("Years to AIDS");round((quantile(infectPattern$toAIDs, na.rm = TRUE) + 2)/52, 2)}else print("No AIDs")
if(length(which(!is.na(infectPattern$toDeath))) != 0){ paste("Years to AIDS Death"); round((quantile(infectPattern$toAIDs + infectPattern$toDeath, na.rm = TRUE)+2)/52,2)}else paste("No AIDs Deaths")
if(length(which(!is.na(infectPattern$toDeath))) != 0){ paste("Years to Death from AIDs"); round(quantile(infectPattern$toDeath, na.rm = TRUE)/52,2)} else paste ("No AIDs Deaths")
if(length(which(!is.na(infectPattern$toDiscovery))) != 0){paste("Years to discovery"); round(quantile(infectPattern$toDiscovery, na.rm = TRUE)/52,2)}else {paste("No Discovery")}
paste("Overall survival (Years)"); round(quantile(infectPattern$Infection_Duration, na.rm = TRUE)/52,2)
paste("Mean infections per year infected: ", round(mean(infectPattern$InfPerYear, na.rm = TRUE), 2))
paste("Expected infections per infected individual: ",  round(mean(infectPattern$InfPerYear, na.rm = TRUE) * (mean(infectPattern$Infection_Duration, na.rm = TRUE)/52),2))
#Is knowledge power? Split up pre and post discovery infections 

