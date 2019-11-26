from commons.schema import GithubAvroSchemaRetriever, FileAvroSchemaRetriever

if __name__ == '__main__':
    GithubAvroSchemaRetriever(
        kwargs={'repo_owner': 'RADAR-base', 'repo_name': 'RADAR-Schemas', 'branch': 'dev', 'basepath': 'commons',
                'extension': '.avsc'})
