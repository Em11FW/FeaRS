library(arules)
args <- commandArgs(TRUE)
support <- as.double(0.000008)
confidence <- as.double(0.5)
maxLength <- as.double(2)
fileName <- args[1] #The file where the commits are stored
transactions<-read.transactions(fileName, sep=',', rm.duplicates=FALSE)
m<-""
m<-apriori(transactions,parameter=list(supp=support,conf=confidence,maxlen=maxLength,minlen=2))

#The output file where the rules will be written, like: ./transactions-1.csv
outputFile <- paste('./',args[2],'-',3,'.csv', sep='')
m.sorted <- sort(m, by='lift')
write(m.sorted,file=outputFile)
