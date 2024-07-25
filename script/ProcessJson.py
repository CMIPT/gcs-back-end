import argparse
import json
from types import SimpleNamespace

def loadJsonAsObject(file_path: str):
    with open(file_path, 'r', encoding='utf-8') as file:
        data = json.load(file)
    
    # transmit dictronary into object
    return json.loads(json.dumps(data), object_hook=lambda d: SimpleNamespace(**d))

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Process JSON file.")
    parser.add_argument('file_path', nargs='?', default='../config.json', help="Path to the JSON file")
    args = parser.parse_args()
    a = loadJsonAsObject(args.file_path)
    
    print(f"name: {a.name}")
    print(f"sex: {a.sex}")
    print(f"age: {a.age}")
