#!/usr/bin/env python3

import pandas as pd
from sqlalchemy import create_engine
import sys
import os.path 

DATABASE_HOST=""
DATABASE_PORT=""
DATABASE_DATABASE=""
DATABASE_USERNAME=""
DATABASE_PASSWORD=""
TARGET_DIRECTORY=""

args = sys.argv[1:]

def help():
    print("required arguments:")
    print("    -h <database host>")
    print("    -p <database port>")
    print("    -d <database db name>")
    print("    -u <database username>")
    print("    -pw <database password>")
    print("optional arguments:")
    print("    -t <target directory>")
if('--help' in args):
    help()
    exit(0)

if(len(args)<10):
    print("Err: Missing arguments")
    help()
    exit(-1)

for i in range(0,len(args)):
    if(args[i] =='-h'):
        DATABASE_HOST = args[i+1]
    if(args[i] =='-p'):
        DATABASE_PORT = args[i+1]
    if(args[i] =='-d'):
        DATABASE_DATABASE = args[i+1]
    if(args[i] =='-u'):
        DATABASE_USERNAME = args[i+1]
    if(args[i] =='-pw'):
        DATABASE_PASSWORD = args[i+1]
    if(args[i] =='-t'):
        TARGET_DIRECTORY = args[i+1]
if(len(TARGET_DIRECTORY)>0):
    if(os.path.isdir(TARGET_DIRECTORY)==False):
        print("Specified path doesn't exist")
        exit(-1)
TARGET_DIRECTORY = os.path.realpath(TARGET_DIRECTORY)+'/'
engine = create_engine('postgresql://'+DATABASE_USERNAME+
                       ':'+DATABASE_PASSWORD+'@'
                       +DATABASE_HOST
                       +':'+DATABASE_PORT+'/'
                       +DATABASE_DATABASE)
print('Getting transactions from database...')
query = "SELECT * FROM transactions"
df = pd.read_sql(query,con=engine)
print('writing ' + str(len(df)) + ' records to disk')
df.to_csv(TARGET_DIRECTORY+'transactions.csv')

print('Getting blocks from database...')
query = "SELECT * FROM blocks"
df = pd.read_sql(query,con=engine)
print('writing ' + str(len(df)) + ' records to disk')
df.to_csv(TARGET_DIRECTORY+'blocks.csv')