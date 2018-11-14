# TeXoo – A Zoo of Text Extractors

TeXoo is a framework for Deep Learning based text analytics in Java developed at DATEXIS, Beuth University of Applied Sciences Berlin. TeXoo comes with a NLP-style document model and a zoo of Deep Learning extraction models which you can access in texoo-models module. Here is a brief overview:


## Features

- Java Framework for language-independent text extraction
- Language-independent document model
- Convenient document readers with tokenization and sentence splitting
- Named Entity Recognition
- Named Entity Linking
- Topic Classification and Segmentation


## Getting Started

These instructions will get you a copy of TeXoo up and running on your local machine for development and testing purposes. If you are going to use TeXoo as a Maven dependency only, you might skip this step.

### Prerequisites

TeXoo comes with a Dockerfile that contains all software necessary to run on most systems, including the CUDA 10 toolkit for GPUs.

- **Docker** Container platform  
<https://docs.docker.com/install/>

- **nvidia-docker v2** for CUDA 10 support  
<https://github.com/nvidia/nvidia-docker/wiki/Installation-(version-2.0)>

The following dependencies are required if you are planning to run TeXoo locally. They are already contained in the Dockerfile:

- **Oracle Java 8 JDK** or **OpenJDK 8 JDK**
- **Apache Maven** Build system for Java  
<https://maven.apache.org/guides/index.html>

### Installation

First we need to build a docker image with all dependencies:

- run ```docker build -t texoo .```

And then we're ready to build TeXoo from source:

- run ```bin/run-docker texoo-build```


## Usage

### Command Line

There exist several run scripts in the `bin/` directory. You can start them right in the docker container, e.g. run all JUnit tests:

- run ```bin/run-docker texoo-test```

See the Modules Overview for more examples.

### Maven Dependency

To use TeXoo in your Java project, just add the following dependencies to your `pom.xml`:

```
<dependency>
  <groupId>de.datexis</groupId>
  <artifactId>texoo-core</artifactId>
  <version>1.0.0-stable</version>
  <type>jar</type>
</dependency>
<dependency>
  <groupId>de.datexis</groupId>
  <artifactId>texoo-entity-recognition</artifactId>
  <version>1.0.0-stable</version>
  <type>jar</type>
</dependency>
<dependency>
  <groupId>de.datexis</groupId>
  <artifactId>texoo-entity-linking</artifactId>
  <version>1.0.0-stable</version>
  <type>jar</type>
</dependency>
<dependency>
  <groupId>de.datexis</groupId>
  <artifactId>texoo-sector</artifactId>
  <version>1.0.0-stable</version>
  <type>jar</type>
</dependency>

```

See the `examples` modules for some implementation examples.


## Modules Overview


### **texoo-core** – Document Model and Core Library

<p align="center"><img src="doc/texoo_model_document.png" width="80%"></p>

| Package / Class                             | Description                                   |
| ------------------------------------------- | --------------------------------------------- |
| **de.datexis.model**.*                      | TeXoo Document model (see below)              |
| **de.datexis.encoder**.*                    | Implementations of Bag-of-words, Word2Vec, Trigrams, etc. |
| de.datexis.preprocess.**DocumentFactory**   | Factory to create Document objects from text  |
| de.datexis.annotator.**AnnotatorFactory**   | Factory to create and load models from the zoo |
| de.datexis.common.**ObjectSerializer**      | Helper methods to import/export JSON          |
	

### **texoo-entity-recognition** NER implementation using Deeplearning4j

This module contains Annotators for **Named Entity Recognition (NER)**. This is a very robust deep learning model that can be trained with only 4000-5000 sentences. It is based on a bidirection LSTM with Letter-trigram encoding, see <http://arxiv.org/abs/1608.06757>.

#### Command Line Usage:

- run ```bin/run-docker texoo-train-ner```

```
usage: texoo-train-ner -i <arg> [-l <arg>] -o <arg> [-t <arg>] [-u] [-v
       <arg>]
TeXoo: train MentionAnnotator with CoNLL annotations
 -i,--input <arg>        path to input training data (CoNLL format)
 -l,--language <arg>     language to use for sentence splitting and
                         stopwords (EN or DE)
 -o,--output <arg>       path to create and store the model
 -t,--test <arg>         path to test data (CoNLL format)
 -u,--ui                 enable training UI (http://127.0.0.1:9000)
 -v,--validation <arg>   path to validation data (CoNLL format)
```

- run ```bin/run-docker texoo-train-ner-seed```

```
usage: texoo-train-ner-seed -i <arg> -o <arg> -s <arg> [-u]
TeXoo: train MentionAnnotator with seed list
 -i,--input <arg>    path and file name pattern for raw input text
 -o,--output <arg>   path to create and store the model
 -s,--seed <arg>     path to seed list text file
 -u,--ui             enable training UI (http://127.0.0.1:9000)
```

#### Java Classes:

| Package / Class                               | Description / Reference                                                |
| --------------------------------------------- | ---------------------------------------------------------------------- |
| de.datexis.ner.**MentionAnnotator**    | Named Entity Recognition |
| de.datexis.ner.**GenericMentionAnnotator**   | Pre-trained models for English and German |

#### Cite

If you use this module for research, please cite:

> Sebastian Arnold, Felix A. Gers, Torsten Kilias, Alexander Löser: Robust Named Entity Recognition in Idiosyncratic Domains. arXiv:1608.06757 [cs.CL] 2016


### **texoo-entity-linking** NEL implementation using Deeplearning4j

This module contains the Annotators for **Named Entity Linking (NEL)** (currently under development). There is no model included, but you can use the Knowledge Base and Annotators with your own datasets, see <https://www.aclweb.org/anthology/C/C16/C16-2024.pdf>.

| Package / Class                               | Description / Reference                                                |
| --------------------------------------------- | ---------------------------------------------------------------------- |
| de.datexis.nel.**NamedEntityAnnotator**    | Named Entity Linking used in TASTY |
| de.datexis.index.**ArticleIndexFactory**   | Knowledge Base implemented as local Lucene Index which imports Wikidata entities |

If you use this module for research, please cite:

> Sebastian Arnold, Robert Dziuba, Alexander Löser: TASTY: Interactive Entity Linking As-You-Type. COLING (Demos) 2016: 111–115


### **texoo-sector** – topic classification and text segmentation using LSTM

Annotators for **SECTOR** models from WikiSection dataset (currently under development)

| Package / Class                               | Description / Reference                                                |
| --------------------------------------------- | ---------------------------------------------------------------------- |
| de.datexis.sector.**SectorAnnotator**      | Topic Segmentation and Classification for Long Documents               |

#### Command Line Usage:

- run ```bin/run-docker texoo-train-sector```

```
usage: texoo-train-sector -i <arg> -o <arg> [-u]
TeXoo: train SectorAnnotator from WikiSection dataset
 -i,--input <arg>    file name of WikiSection training dataset
 -o,--output <arg>   path to create and store the model
 -u,--ui             enable training UI (http://127.0.0.1:9000)

```


## About TeXoo

### Frameworks used in TeXoo

- **Deeplearning4j** Machine learning library  
<http://deeplearning4j.org/documentation>
- **ND4J** Scientific computing library  
<http://nd4j.org/userguide>
- **Stanford CoreNLP** Natural language processing  
<http://stanfordnlp.github.io/CoreNLP/>

### Contributors

Sebastian Arnold @sebastianarnold – core developer
<https://prof.beuth-hochschule.de/loeser/people/sebastian-arnold/>

Rudolf Schneider @SchmaR
<https://prof.beuth-hochschule.de/loeser/people/rudolf-schneider/>


## License

   Copyright 2015-2018 Sebastian Arnold, Alexander Löser, Rudolf Schneider

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
