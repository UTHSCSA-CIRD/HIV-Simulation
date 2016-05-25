library(ggplot2)
library(reshape2)
library(car)#recode
agentLog = read.table("agentLog.txt", header = TRUE, sep = "\t")
eventLog = read.table("eventLog.txt", header = TRUE, sep = "\t")
yearLog = read.table("yearLog.txt", header = TRUE, sep = "\t")
#heteroInfect = eventLog[(eventLog$Action == "New Heterosexual Infection"),"Agent.1"]
#homoInfect = eventLog[(eventLog$Action == "New Homosexual Infection"),"Agent.1"]
sexualInfect = eventLog[(eventLog$Action == "New Heterosexual Infection")|(eventLog$Action == "New Homosexual Infection"),"Agent.1"]
rNot = table(sexualInfect)
#rNotHomo = table(homoInfect)
#rNotHetero = table(heteroInfect)
yearLog$percentInfected = yearLog$Prevelance/yearLog$Starting.Population
yearLog$incidenceRate = yearLog$Incidence/yearLog$Starting.Population
highR0 = as.data.frame(rNot[rNot>2])
iAgents = agentLog[agentLog$ID%in%rownames(highR0),]
#changing this to grep for 'sexual' to avoid mother to child infections.
infAgents = agentLog[agentLog$ID%in%eventLog[grepl("*sexual*",eventLog$Action), "Agent"],]
lmAgents = agentLog
lmAgents$Infected = agentLog$ID%in%eventLog[grepl("*sexual*",eventLog$Action), "Agent"]
popGrowthRate = log(yearLog[nrow(yearLog), "Starting.Population"]/yearLog[1,"Starting.Population"])/nrow(yearLog)
agentLog$CCR5 = interaction(agentLog$CCR51, agentLog$CCR52, drop = TRUE)
agentLog$CCR2 = interaction(agentLog$CCR21, agentLog$CCR22, drop = TRUE)
agentLog$HLA_A = interaction(agentLog$HLA_A1, agentLog$HLA_A2, drop = TRUE)
agentLog$HLA_B = interaction(agentLog$HLA_B1, agentLog$HLA_B2, drop = TRUE)
agentLog$HLA_C= interaction(agentLog$HLA_C1, agentLog$HLA_C2, drop = TRUE)
agentLog$CCR5 = recode(agentLog$CCR5, "'CCR5 Haplotype HHE.CCR5 Delta 32' = 'CCR5 Delta 32.CCR5 Haplotype HHE';'CCR5 Wild Type.CCR5 Haplotype HHE' = 'CCR5 Haplotype HHE.CCR5 Wild Type';'CCR5 Wild Type.CCR5 Delta 32'= 'CCR5 Delta 32.CCR5 Wild Type'")
agentLog$CCR2 = recode(agentLog$CCR2, "'CCR2 Wild Type.CCR2 V64I' = 'CCR2 V64I.CCR2 Wild Type'")
agentLog$HLA_A = recode(agentLog$HLA_A, "'HLA A01 A03.HLA A01' = 'HLA A01.HLA A01 A03'; 'HLA A01 A03.HLA A01 A24'='HLA A01 A24.HLA A01 A03'; 'HLA A01 A24.HLA A01'='HLA A01.HLA A01 A24';
                        'HLA A01.HLA A02 '='HLA A02.HLA A01';'HLA A01 A24.HLA A24'='HLA A02.HLA A01 A24';'HLA A01 A03.HLA A02'='HLA A02.HLA A01 A03';
                        'HLA A01 A03.HLA A03'='HLA A03.HLA A01 A03';'HLA A01 A24.HLA A02'='HLA A03.HLA A01 A24';'HLA A01.HLA A03'='HLA A03.HLA A01';
                        'HLA A02.HLA A03'='HLA A03.HLA A02';'HLA A01 A03.HLA A24'='HLA A24.HLA A01 A03';'HLA A01 A24.HLA A03'='HLA A24.HLA A01 A24';
                        'HLA A01.HLA A24'='HLA A24.HLA A01';'HLA A02.HLA A24'='HLA A24.HLA A02';'HLA A03.HLA A24'='HLA A24.HLA A03'")
#Single Variable Plots
plot(yearLog$Year, yearLog$incidenceRate, type = "l")
plot(yearLog$Year, yearLog$percentInfected, type = "l")
plot(yearLog$Starting.Population, type = "l")
#Multivariable plots
tmp = yearLog[, c("Year","Starting.Population","Prevelance")];melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line() 
tmp = yearLog[, c("Year","percentInfected","incidenceRate")];tmp$mortalityRate = (yearLog$Mortality/yearLog$Starting.Population);melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line() 
tmp = yearLog[, c("Year","Birth.Rate","Mortality")];tmp$NaturalDeath = (yearLog$Death.Rate - yearLog$Mortality);melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line() 
#Genes Over time
ggplot(agentLog,aes(Entry.Step, fill = CCR5)) + geom_area(stat = "bin", position = "fill")
ggplot(agentLog,aes(Entry.Step, fill = CCR2)) + geom_area(stat = "bin", position = "fill")
#ggplot(agentLog,aes(Entry.Step, fill = HLA_A)) + geom_area(stat = "bin", position = "fill")

#resistance
ggplot(agentLog,aes(Entry.Step, fill = as.factor(CCR5Factor))) + geom_area(stat="bin", position = "fill")
ggplot(agentLog,aes(Entry.Step, fill = as.factor(HLAFactor))) + geom_area(stat="bin", position = "fill")
#Rnot and t-test- Compare population profiles for non-infected, infected, and high Rnot
paste("Mean rNot, sexual transmission:", mean(rNot))
#paste("Mean rNot, Heterosexual transmission:",mean(rNotHetero))
#paste("Mean rNot, Homosexual transmission:",mean(rNotHomo))
paste("Max rNot:", max(rNot))
#heterosexual vs homosexual infection
#paste("HeteroSexual Infection:", length(heteroInfect)/(length(sexualInfect)), "Homosexual Infection:", length(homoInfect)/(length(sexualInfect)))
summary(agentLog)
summary(infAgents)
summary(iAgents)
#all infected vs non infected 
t.test(infAgents$Faithfulness, agentLog$Faithfulness)
t.test(infAgents$Want, agentLog$Want)
t.test(infAgents$Condom.Usage, agentLog$Condom.Usage)
t.test(infAgents$CCR5Factor, agentLog$CCR5Factor)
t.test(infAgents$HLAFactor, agentLog$HLAFactor)
#t.test(infAgents$Selectivity, agentLog$Selectivity)
#all agents and high infectors
t.test(agentLog$Faithfulness, iAgents$Faithfulness)
t.test(agentLog$Want, iAgents$Want)
t.test(agentLog$Condom.Usage, iAgents$Condom.Usage)
t.test(agentLog$CCR5Factor, iAgents$CCR5Factor)
t.test(agentLog$HLAFactor, iAgents$HLAFactor)
#t.test(agentLog$Selectivity, iAgents$Selectivity)
#infected agents (minus initial infected who were randomly selected) and high infectors
t.test(infAgents$Faithfulness, iAgents$Faithfulness)
t.test(infAgents$Want, iAgents$Want)
t.test(infAgents$Condom.Usage, iAgents$Condom.Usage)
t.test(infAgents$CCR5Factor, iAgents$CCR5Factor)
t.test(infAgents$HLAFactor, iAgents$HLAFactor)
#t.test(infAgents$Selectivity, iAgents$Selectivity)

