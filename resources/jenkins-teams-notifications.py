#!/usr/bin/env python3
import os
import json
import requests
import sys
import argparse

def form_payload(build_number, job_name, build_url, build_result):
    """Forms the python representation of the data payload to be sent to Teams"""
    payload_rep = {
        "title": f"{job_name} - Build #{build_number} - {build_result}",
        "description": f"Job {job_name} foi finalizado como {build_result}, verifique o console output para ver os resultados.",
        "url": build_url,
        "buildResult": build_result
    }
    return payload_rep

def post_to_url(url, payload, auth_token):  
    """Posts the formed payload as json to the passed url"""
    try:
        headers = {
            'Authorization': f'Bearer {auth_token}',
            'Content-Type': 'application/json'
        }
        req = requests.post(url, data=json.dumps(payload), headers=headers)
        if req.status_code > 299:
            print(f"Request failed with status code {req.status_code}: {req.content}")
        else:
            print(f"Successfully sent notification to Teams")
    except requests.exceptions.RequestException as e:
        print(f"Unable to send notification to Teams: {e}")
        sys.exit(2)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Sending Teams notifications for Jenkins builds.')
    parser.add_argument('--url', help='Teams webhook URL', default=os.environ.get('TEAMS_URL'))
    parser.add_argument('--token', help='Teams authorization token', default=os.environ.get('TEAMS_TOKEN'))
    parser.add_argument('--build-number', help='Jenkins Build Number', default=os.environ.get('BUILD_NUMBER'))
    parser.add_argument('--job-name', help='Jenkins Job Name', default=os.environ.get('JOB_NAME'))
    parser.add_argument('--build-url', help='Jenkins Build URL', default=os.environ.get('BUILD_URL'))
    parser.add_argument('--build-result', help='Jenkins Build Result', default=os.environ.get('BUILD_RESULT'))
    
    args = parser.parse_args()
    
    if not args.url:
        print("Teams URL not provided")
        sys.exit(1)
    
    if not args.token:
        print("Teams authorization token not provided")
        sys.exit(1)
    
    post_to_url(
        args.url, 
        form_payload(args.build_number, args.job_name, args.build_url, args.build_result),
        args.token
    )
