FROM azul/zulu-openjdk:13

ARG DEBIAN_FRONTEND=noninteractive

# Install wget and make
### We need make to install R packages from source
### We need 'wget' and 'libarchive-dev' to download and install SrcML
RUN apt-get update \
    && apt-get install -y wget build-essential libarchive-dev

# Install R according to https://cran.r-project.org/ > R-Binaries > Linux > Ubuntu
#### We need 'r-base-dev' to install rJava package
RUN apt install -y --no-install-recommends software-properties-common dirmngr \
    && apt install -y gpg-agent \
    && apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9 \
    && add-apt-repository "deb https://cloud.r-project.org/bin/linux/ubuntu $(lsb_release -cs)-cran40/" \
    && apt install -y --no-install-recommends r-base r-base-dev\
    && R CMD javareconf \
    && R -e "install.packages(c('rJava','arules'))"

ENV R_HOME=/usr/lib/R
ENV JAVA_OPTS="-Djava.library.path=/usr/local/lib/R/site-library/rJava/jri"

# Install SrcML
ENV SRCML_DEB_URL http://131.123.42.38/lmcrs/v1.0.0/srcml_1.0.0-1_ubuntu18.04.deb
RUN wget $SRCML_DEB_URL \
    && dpkg -i srcml_1.0.0-1_ubuntu18.04.deb


# Copy stuff
WORKDIR /usr/local/
RUN mkdir fears-app
COPY build/libs/feaRS-1.0-SNAPSHOT.jar ./fears-app/fears.jar
COPY resources/asia-resources ./fears-app/resources/asia-resources

# Entry point commands
ENTRYPOINT sleep 10 && java $JAVA_OPTS -jar fears-app/fears.jar $CLONED_REPOS_PATH $TEMP_XML_PATH $EXTRACTED_METHODS_PATH $CLUSTER_OP_PATH $CLUSTER_THREADS $CLUSTER_THRESHOLD $CLUSTER_TRIGGER_MIN_N_METHOD $ARULE_SUPPORT $ARULE_CONFIDENCE $ARULE_MAXLEN $DB_URL $DB_NAME $DB_USER $DB_PASS
