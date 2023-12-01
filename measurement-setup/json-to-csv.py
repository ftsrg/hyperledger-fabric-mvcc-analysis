import os
import json
import argparse
import pandas

# input file, output file, separator
parser = argparse.ArgumentParser()
parser.add_argument("dir", help="The directory path where the input file is")
parser.add_argument("type", help="The type of data to convert. Either txinfo or blockinfo")
parser.add_argument("-s", "--separator", help="The separator character to use for the CSV", default=",")

args = parser.parse_args()

json_file_path = os.path.join(args.dir, "{0}.txt".format(args.type))
csv_file_path = os.path.join(args.dir, "{0}.csv".format(args.type))

if not os.path.isfile(json_file_path):
    print("JSON file path is incorrect: {0}".format(json_file_path))
    exit(1)

if os.path.isfile(csv_file_path):
    print("CSV file already exists, will be overwritten: {0}".format(csv_file_path))

df = pandas.DataFrame()

print("Building dataframe from {0}".format(json_file_path))

with open(json_file_path) as json_file:
    batch = []
    for line in json_file:
        batch.append(json.loads(line)[args.type])

        if len(batch) >= 10000:
            df = df.append(batch, ignore_index=True, sort=True)
            batch = []

    if len(batch) > 0:
        df = df.append(batch, ignore_index=True, sort=True)
        batch = []


df.sort_index(axis=1, inplace=True)

print("Dataframe size: {0}".format(len(df)))
print("Saving CSV to {0}".format(csv_file_path))
df.to_csv(csv_file_path, sep=args.separator, index=False)
