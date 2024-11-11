![תמונה](https://github.com/user-attachments/assets/3ee4a530-ad48-45c1-831c-9b9d87a85aaa)
CouponVerse is the complete Android app solution for all your coupon needs.  
Co-created with Dor Sror. 

## Features
- Product Recommendation ML Model (trained from scratch)
- Coupons AI Text Analyzer
- Online Groups and Chats
- Coupons Organizer

## Architecture 
Frontend: Kotlin (Android)

Backend: ASP.NET (C#) & Flask (Python) - Implemented MVC RESTful API paradigm

Database: MongoDB 

Word Embeddor: Sentence Transformers (SBERT) 

ML Model: sklearn K-MEANS 

ML Dataset: Amazon Products (from Kaggle.com) 

AI Assistant: Google’s Gemini AI 

## ML Model Overview  
There are 3 KMeans models with varying numbers of centers: kmeans150.pkl, kmeans1000.pkl and kmeans6500.pkl are models which have 150/1000/6500 centers respectively.  
The more centers the model has, the more accuracy the recommendations are. However, there will be less recommendations given.  
For example, kmeans6500 can be used to provide highly accurate recommendations but will yield fewer than 200 recommendations, whereas kmeans150 is much less accurate but will provide over 10,000 recommendations.  
Each model can be used separately to give more (or less) precise recommendations or can be used in tandem to show more precise recommendations first, followed by less accurate ones.
![תמונה](https://github.com/user-attachments/assets/0262c674-197b-404f-90ef-862c62dfb8f7)

## Dependencies
### ASP.Net Server  
MongoDB.Driver (MongoDB integration)  ,Mscc.GenerativeAI (Google Gemini AI integration)  
### Android App 
Glide,Gson,kotlinx-coroutines,okhttp,,retrofit2
### Python
sklearn.cluster, sentence_transformers (sbert), flask, pandas, numpy 1.26.1, dill

## Running
1. Start MongoDB, and import the sample data (optional)
2. Start the Server_API with `dotnet run`
3. Start Server_ML_Models with `python3 app.py`
4. Start the android app (preferably with android studio)





