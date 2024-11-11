from sentence_transformers import SentenceTransformer
import pandas as pd
from sklearn.cluster import KMeans # USE SKLEARN VERSION 1.5.0
import numpy as np # USE NUMPY VERSION 1.26.1
#import math
#import pickle
#import torch
import dill
import random

# dataset is split to several parts due to it's size
df_products = pd.read_csv(f"amazon_products_part_0.csv")
for i in [1, 2, 3, 4]:
    curr_part = pd.read_csv(f"amazon_products_part_{i}.csv")
    df_products = pd.concat([df_products, curr_part])


df_categories = pd.read_csv("amazon_categories.csv")

# joining category name to each product based on category id
df_categories.rename(columns={"id":"category_id"},inplace=True)
df_products = pd.merge(df_products,df_categories,how='left',on='category_id')

# load sentence transformer model
sentence_transformer = SentenceTransformer("all-MiniLM-L6-v2")


# To load a model:
try:
    with open('kmeans150.pkl', 'rb') as f:
        kmeans150 = dill.load(f)
except FileNotFoundError:
    print("File not found.")
else:
    f.close()

try:
    with open('kmeans6500.pkl', 'rb') as f:
        kmeans6500 = dill.load(f)
except FileNotFoundError:
    print("File not found.")
else:
    f.close()

try:
    with open('kmeans1000.pkl', 'rb') as f:
        kmeans1000 = dill.load(f)
except FileNotFoundError:
    print("File not found.")
else:
    f.close()

def get_embedding(input):
    return sentence_transformer.encode(input).flatten().astype(np.float64)


def get_similar_products(kmeans_size,embeddings,result_size=50,random=True):
    if kmeans_size == 150:
        model = kmeans150
    elif kmeans_size == 1000:
        model = kmeans1000
    elif kmeans_size == 6500:
        model = kmeans6500
    else:
        raise Exception("Wrong kmeans size. Correct values are: 150/1000/6500")
    
    embeddings = embeddings.reshape(1, -1).astype(np.float64)
    # get predicted cluster
    predicted_cluster = model.predict(embeddings)[0]
    cluster_indices = np.where(model.labels_ == predicted_cluster)[0]

    # Retrieve similar products from the same cluster
    similar_products = df_products.iloc[cluster_indices]
    if random:
        similar_products = similar_products.sample(frac=1)
    
    try:
        similar_products = similar_products[0:result_size]

    # case when not enough results
    except:
        similar_products = similar_products.head(0)
    return similar_products


