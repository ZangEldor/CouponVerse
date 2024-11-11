import spacy
import json
from flask import Flask, request, jsonify
import inference
import numpy as np
import pandas as pd
# Load SpaCy's pre-trained NER model
nlp = spacy.load("en_core_web_sm")

# return size for each model
return_size_by_kmeans = {150: 10,
                         1000: 15,
                         6500: 75}


# Initialize Flask application
app = Flask(__name__)

@app.route('/embedding', methods=['POST'])
def embedding():
    data = request.get_json()
    input = data.get('input')
    embedding = inference.get_embedding(input)
    embedding_list = embedding.tolist()
    # Return the extracted tokens and entities as JSON
    return jsonify({'embedding_list': embedding_list})

@app.route('/NER', methods=['POST'])
def NER():
    data = request.get_json()
    sentence = data.get('input')
    
    # Process the sentence using SpaCy
    doc = nlp(sentence)
    
    # Extract entities
    entities = [{'text': ent.text, 'label': ent.label_} for ent in doc.ents]
    # Return the extracted tokens and entities as JSON
    return jsonify({'entities': entities})

@app.route('/MLRecommendationsModel', methods=['POST'])
def MLRecommendationsModel():
    data = request.get_json()
    embeddings = np.array(data, dtype=np.float64)
    for kmeans_size,return_size in [(k,v) for k,v in return_size_by_kmeans.items()]:
        curr_df = inference.get_similar_products(kmeans_size, embeddings,return_size, random=True)
        try:
            df = pd.concat([df,curr_df])
        # case for first iteration
        except:
            df = curr_df.copy()
    return jsonify(df.to_dict(orient='records'))

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=6000)
