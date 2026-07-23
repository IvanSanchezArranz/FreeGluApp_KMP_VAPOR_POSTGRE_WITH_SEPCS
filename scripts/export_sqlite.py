import os
import sqlite3
import pandas as pd
import uuid

# Paths
CSV_PATH = "../data/foods.csv"
DB_NAME = "freeglu.db"
ANDROID_ASSETS_DIR = "../FreeGluKMP/androidApp/src/main/assets"
IOS_RESOURCES_DIR = "../FreeGluKMP/iosApp/iosApp"

def create_local_sqlite():
    print("Creating SQLite local database schema...")
    conn = sqlite3.connect(DB_NAME)
    cursor = conn.cursor()
    
    # Create the Food table exactly as defined in SQLDelight schema
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS Food (
        id TEXT NOT NULL PRIMARY KEY,
        code TEXT NOT NULL,
        name TEXT NOT NULL,
        brand TEXT NOT NULL,
        categories TEXT NOT NULL,
        ingredients TEXT NOT NULL,
        imageUrl TEXT NOT NULL,
        isGlutenFree INTEGER NOT NULL
    );
    """)
    conn.commit()
    return conn

def populate_database(conn):
    print("Reading foods.csv and filtering for gluten-free products...")
    
    USEFUL_COLUMNS = [
        "code",
        "product_name",
        "brands",
        "categories_en",
        "ingredients_text",
        "image_url",
        "labels_tags"
    ]
    
    # Process only the first few chunks to keep the db lightweight (around 500-1000 high-quality products)
    chunk_size = 10000
    max_foods = 1000
    foods_inserted = 0
    
    cursor = conn.cursor()
    
    try:
        reader = pd.read_csv(
            CSV_PATH,
            sep="\t",
            chunksize=chunk_size,
            encoding="utf-8",
            low_memory=False,
            on_bad_lines="skip",
            usecols=USEFUL_COLUMNS
        )
        
        for i, chunk in enumerate(reader):
            print(f"Processing chunk {i+1}...")
            
            # Filter for gluten-free
            chunk["labels_tags"] = chunk["labels_tags"].fillna("")
            is_gf = chunk["labels_tags"].str.contains("gluten-free", case=False)
            gf_chunk = chunk[is_gf].copy()
            
            # Clean empty names
            gf_chunk = gf_chunk.dropna(subset=["product_name"])
            
            if len(gf_chunk) == 0:
                continue
                
            # Fill other NaNs with empty string
            gf_chunk["brands"] = gf_chunk["brands"].fillna("Generic")
            gf_chunk["categories_en"] = gf_chunk["categories_en"].fillna("Uncategorized")
            gf_chunk["ingredients_text"] = gf_chunk["ingredients_text"].fillna("No ingredients list available.")
            gf_chunk["image_url"] = gf_chunk["image_url"].fillna("")
            gf_chunk["code"] = gf_chunk["code"].fillna("")
            
            # Insert rows
            for _, row in gf_chunk.iterrows():
                product_id = str(uuid.uuid4())
                cursor.execute("""
                INSERT OR REPLACE INTO Food (id, code, name, brand, categories, ingredients, imageUrl, isGlutenFree)
                VALUES (?, ?, ?, ?, ?, ?, ?, 1)
                """, (
                    product_id,
                    str(row["code"]),
                    row["product_name"],
                    row["brands"],
                    row["categories_en"],
                    row["ingredients_text"],
                    row["image_url"]
                ))
                foods_inserted += 1
                if foods_inserted >= max_foods:
                    break
            
            conn.commit()
            print(f"Inserted {foods_inserted} gluten-free products so far.")
            
            if foods_inserted >= max_foods:
                print("Target food count reached. Stopping import.")
                break
                
    except Exception as e:
        print(f"Error reading CSV or importing: {e}")
        print("Creating fallback mock database...")
        # Create a fallback mock set of products if the CSV fails to load
        create_fallback_data(cursor)
        conn.commit()

def create_fallback_data(cursor):
    mocks = [
        ("1", "8410000000012", "Pan de Molde Sin Gluten", "Schär", "Bread, Bakery", "Water, rice starch, maize starch", "https://images.openfoodfacts.org/images/products/841/000/000/0012/front_es.3.400.jpg"),
        ("2", "8410000000029", "Galletas María Sin Gluten", "Gullón", "Cookies, Sweet", "Corn starch, sugar, high oleic sunflower oil", "https://images.openfoodfacts.org/images/products/841/000/000/0029/front_es.3.400.jpg"),
        ("3", "8410000000036", "Cerveza Especial Sin Gluten", "Daura Damm", "Beverages, Beers", "Water, barley malt (gluten removed)", "https://images.openfoodfacts.org/images/products/841/000/000/0036/front_es.3.400.jpg"),
        ("4", "8410000000043", "Pasta Penne Sin Gluten", "Gallo", "Pasta, Penne", "White corn flour, yellow corn flour, rice flour", ""),
        ("5", "8410000000050", "Picos Rústicos Sin Gluten", "Airos", "Bakery, Snacks", "Tapioca starch, potato starch, extra virgin olive oil", ""),
        ("6", "8410000000067", "Tortitas de Arroz con Chocolate", "Bicentury", "Snacks, Sweet", "Rice, sugar, cocoa butter", ""),
        ("7", "8410000000074", "Masa de Hojaldre Sin Gluten", "Buitoni", "Bakery, Doughs", "Potato starch, water, butter", ""),
        ("8", "8410000000081", "Bizcocho Mármol Sin Gluten", "Adpan", "Cakes, Sweet", "Egg, sugar, rice flour, cocoa powder", ""),
        ("9", "8410000000098", "Pan de Hamburguesa Sin Gluten", "Proceli", "Bread, Buns", "Water, rice starch, sunflower oil", ""),
        ("10", "8410000000104", "Galletas Choco Chips Sin Gluten", "NaturSnack", "Cookies, Sweet", "Buckwheat flour, chocolate chips, coconut sugar", "")
    ]
    for m in mocks:
        cursor.execute("""
        INSERT OR REPLACE INTO Food (id, code, name, brand, categories, ingredients, imageUrl, isGlutenFree)
        VALUES (?, ?, ?, ?, ?, ?, ?, 1)
        """, m)

def copy_to_platforms():
    print("Copying freeglu.db to mobile platforms...")
    
    # Android Assets copy
    if not os.path.exists(ANDROID_ASSETS_DIR):
        os.makedirs(ANDROID_ASSETS_DIR)
    
    android_dest = os.path.join(ANDROID_ASSETS_DIR, DB_NAME)
    import shutil
    shutil.copy2(DB_NAME, android_dest)
    print(f"Copied to Android Assets: {android_dest}")
    
    # iOS Resources copy
    if os.path.exists(IOS_RESOURCES_DIR):
        ios_dest = os.path.join(IOS_RESOURCES_DIR, DB_NAME)
        shutil.copy2(DB_NAME, ios_dest)
        print(f"Copied to iOS Resources: {ios_dest}")
    else:
        print(f"Warning: iOS Resources dir {IOS_RESOURCES_DIR} not found, skipped.")

if __name__ == "__main__":
    conn = create_local_sqlite()
    populate_database(conn)
    conn.close()
    copy_to_platforms()
    print("🎉 SQLite generation and copy complete!")
