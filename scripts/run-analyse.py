#!/usr/bin/env python
import subprocess
import os

DIRS = ['struts_1_3_9', 'commons-collections', 'activemq']

ITERATIONS = 10

def run_analyse(directory, branch, logfile, params=''):
    par = {
        'dir': directory,
        'branch': branch,
        'logfile': logfile,
        'params': params,
        }
    cmd = 'cd %(dir)s && mvn sonar:sonar -e -Dsonar.branch=%(branch)s %(params)s > %(logfile)s' % par
    print cmd
    subprocess.call(cmd, shell=True)

def run_old(directory, branch, logfile):
    params = "-Dsonar.newcpd.skip=true" 
    run_analyse(directory, branch, logfile, params)

def run_new(directory, branch, blocksize, logfile, backend):
    params = '-Dsonar.cpd.skip=true'
    params += ' -Dsonar.newcpd.blockSize=%s' % blocksize
    params += ' -Dsonar.newcpd.backend=%s' % backend
    run_analyse(directory, branch, logfile, params)

################
#OLDCPD
################
for directory in DIRS:
    absdir = os.path.join("~/sonar/",directory)
    run_old(absdir, "OLDCPD", "oldcpd.log")

BLOCK_SIZES = [10, 15, 18, 20, 22, 25, 30]
##########################################
#NEWCPD memory - blockSize bruteforce
##########################################
for directory in DIRS:
    absdir = os.path.join("~/sonar/",directory)
    for blocksize in BLOCK_SIZES:
        branch = "NEWCPD-MEMORY-%s" % blocksize
        logfile = "%s.log" % branch.lower()
        run_new(absdir, branch, blocksize, logfile, 'memory')
    
#####################################################
#NEWCPD db - test work time on different iterations
#####################################################
for iteration in range(ITERATIONS):
    print "iteration: %s" % iteration

    for directory in DIRS:
        absdir = os.path.join("~/sonar/",directory)
        blocksize = 20

        branch = "NEWCPD-DB-%s" % blocksize
        logfile = "%s-%s.log" % (branch.lower(), iteration)
        run_new(absdir, branch, blocksize, logfile, 'db')
