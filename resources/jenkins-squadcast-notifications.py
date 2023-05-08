#!/usr/bin/env python3
import os
import json
import requests
import sys
import argparse

def form_payload(build_number, job_name, build_url, status, job_status, priority):
    """Forms the python representation of the data payload to be sent from the passed configuration"""
    message = "Job {} (#{}) - {}".format(job_name, build_number, job_status)
    description = "Job: {}\nBuild Number: {}\nStatus: {}. \nBuild URL: {}".format(job_name, build_number, job_status, build_url)
    payload_rep = {"message" : message , "description" : description,
        "build_url":  build_url, "job_name":  job_name, "build_number":  build_number,
        "status" : status, "event_id" : job_name, "priority": priority}
    return payload_rep

def post_to_url(url, payload):  
    """Posts the formed payload as json to the passed url"""
    try:
        headers={
        'User-Agent': 'squadcast',
        "Content-Type": "application/json"
        }
        req = requests.post(url, data = bytes(json.dumps(payload).encode('utf-8')), headers = headers)
        if req.status_code > 299:
            print("Request failed with status code %s : %s" % (req.status_code, req.content))
    except requests.exceptions.RequestException as e:
            print("Unable to create an incident with Squadcast, ", e)
            sys.exit(2)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Passing build information.')
    parser.add_argument('--url', help='Squadcast API endpoint', default=os.environ.get('SQUADCAST_URL'))
    parser.add_argument('--build-number', help='Jenkins Build Number', default=os.environ.get('BUILD_NUMBER'))
    parser.add_argument('--job-name', help='Jenkins Job Name', default=os.environ.get('JOB_NAME'))
    parser.add_argument('--build-url', help='Jenkins Build URL', default=os.environ.get('BUILD_URL'))
    parser.add_argument('--job-url', help='Jenkins Job URL', default=os.environ.get('JOB_URL'))
    parser.add_argument('--build-status', help='Jenkins Build Status', default=os.environ.get('BUILD_STATUS'))
    parser.add_argument('--priority', help='Squadcast Priority', default="P3")
    args = parser.parse_args()    
    if (args.build_status == "FAILURE") or (args.build_status == "UNSTABLE") or (args.build_status == "ABORTED"):
        post_to_url(args.url, form_payload(str(args.build_number), args.job_name, args.build_url, "trigger", args.build_status, args.priority))
    elif (args.build_status == "SUCCESS"):
        post_to_url(args.url, form_payload(str(args.build_number), args.job_name, args.build_url, "resolve", args.build_status, args.priority))
    elif (args.build_status == "STARTED"):
        post_to_url(args.url, form_payload(str(args.build_number), args.job_name, args.build_url, "trigger", args.build_status, args.priority))
    else:
        print ("Build status not found..")