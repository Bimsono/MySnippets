#!/Users/abimbolaolajubutu/Documents/ENV/bin/python

import os, sys 
import SoftLayer
import urllib3, certifi 
import urllib3.contrib.pyopenssl

#So Jenkins server correctly validates when accessing packages to install for environment
urllib3.contrib.pyopenssl.inject_into_urllib3()
http = urllib3.PoolManager(cert_reqs='CERT_REQUIRED', ca_certs=certifi.where())

'''
NOTES:
All imgs shared starting from csf_dev so no reason for it to be an env option
cmprd encompasses both cm_paris cm_frankfurt datacenters
'''
env_id = {'slops':380790, 'csfdev':366226,'pstg':380586, 'pprd':380786, 'softbankdev':1003195, 'softbankprd':1015135, 'cmprd':1040127, 'moonprd':352970, 
'trprd':1143235, 'ciscoprd':353016, 'skprd':1186685, 'fraprd':380786, 'lonprd':380786, 'moonfraprd':352970}

#Keys assigned to JENKINS params values
'''Top two are injected witin Jenkins job script'''
sl_user                  = os.environ['asgard_sl_username']
sl_api                   = os.environ['asgard_sl_api_key']

sl_image_name            = os.environ['sl_image_name']
sl_env_accts_selected    = os.environ['sl_env_accts']#String of comma-seperated selections
sl_env_datactrs_selected = os.environ['sl_env_datactrs']#String of comma-seperated selections

print "SL_ENV_SELECTED", sl_env_accts_selected 
print "SL_ENV_SELECTED type", type(sl_env_accts_selected)

selected_list = sl_env_datactrs_selected.split(",")

print "SL_DATACTRS_SELECTED", selected_list
print "SL_DATACTRS_SELECTED", type(selected_list)

#Create client
base_client = SoftLayer.create_client_from_env(username=sl_user, api_key=sl_api)

#Get unique identity of sl_image object. Dup sl_image names will/should have timestamps attached that force uniqueness 
media_obj    = SoftLayer.managers.image.ImageManager(base_client)
image_list   = media_obj.list_private_images(name=sl_image_name)
img_identity = image_list[0]

#Share to sl account
'''Access api first to check that account doesn't have sharing access to image, if not then share '''
shared_img = False
sl_img_accts_shared = base_client.call('Virtual_Guest_Block_Device_Template_Group', 'getAccountReferences', id=img_identity['id'])#array of SoftLayer_Virtual_Guest_Block_Device_Template_Group_Accounts
print "SL accts image shared with: ", sl_img_accts_shared


#Create list of selected acct IDs
selectd_acct_ids   = []
selected_acct_list = sl_env_accts_selected.split(",")
for acct in selected_acct_list:
    selectd_acct_ids.append( env_id[acct] )

  
#Prune SELECTD_ACCT_IDS of sl env accounts already shared with img to avoid errors
for acct in sl_img_accts_shared:
    if acct['accountId'] in selectd_acct_ids:
        selectd_acct_ids.remove(acct['accountId'])

#TODO: See if can put list of ids all in one go for sl api
for acct_id in selectd_acct_ids: 
    print "Sharing ", sl_image_name ," with acct_id: ", acct_id
    base_client.call('Virtual_Guest_Block_Device_Template_Group', 'permitSharingAccess', acct_id, id=img_identity['id'])


#Share image with assoc. datacenters
dest_datactrs        = []
sl_datactrs          =  base_client.call('Location', 'getDatacenters')#list of dictionaries on datactrs' info 
selected_env_datacrs = sl_env_datactrs_selected.split(",")

'''Softlayer disregards repeated sharing of image with already shared datacenter. Pre-check for membership not done here
in order to avoid double loop '''
for sl_datactr in sl_datactrs:
    if sl_datactr['name'].strip() in selected_env_datacrs:
        print "data center short name was recognized: ", sl_datactr['name'].strip()
        dest_datactrs.append(sl_datactr)            

if dest_datactrs:
    print "sharing with datacenter(s)"
    print "dest_datactrs list: ", dest_datactrs
    status = base_client.call('Virtual_Guest_Block_Device_Template_Group', 'addLocations', dest_datactrs, id=img_identity['id'])
