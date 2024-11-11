# ML Recommendation Model
This directory contains the data used to train the KMeans models used for the recommendations in the CouponVerse app.

## Overview
There are 3 KMeans models with varying amount of centers - kmeans150.pkl, kmeans1000.pkl and kmeans6500.pkl are models with 150/1000/6500 centers respectively.
The more centers the model has, the more accuracy it has with recommendations but less recommendations given. For example, kmeans6500 can be used to give very close recommendations but will yield less than 200 recommendations, whereas kmeans150 is much less accurate but will yield upward of 10,000 recommendations.
Each model can be used separately to give more (or less) precise recommendations or used in tandem to show more precise recommendations first and then less accurate recommendations.

## Dependencies
- numpy version 1.26.1
- sentence_transformers (latest version)
- dill
- KMeans class from sklearn.cluster
- pandas

# Files

Models:
- kmeans150.pkl	- model with 150 centers.
- kmeans1000.pkl	- model with 1000 centers.
- kmeans6500.pkl	- model with 6500 centers.

Data:
- amazon_categories.csv	- contains list of categories of Amazon products.
- amazon_products_part_[0-4] - contains data entries for the training of the models.
- embeddings.pkl 	- file containing the embeddings of all the given data (pre-calculated).

Jupyter Notebook:
- Product Recommendation Model.ipynb	- Jupyter Notebook file used to train and test the models.