import os
import shutil
import hashlib

def file_hash(path):
    hasher = hashlib.sha256()
    with open(path, 'rb') as f:
        for chunk in iter(lambda: f.read(8192), b''):
            hasher.update(chunk)
    return hasher.hexdigest()

src_dir = './resources'
dst_dir = './test/resources'

for root, dirs, files in os.walk(src_dir):
    rel_dir = os.path.relpath(root, src_dir)
    target_dir = os.path.join(dst_dir, rel_dir) if rel_dir != '.' else dst_dir
    os.makedirs(target_dir, exist_ok=True)
    for file in files:
        src_file = os.path.join(root, file)
        dst_file = os.path.join(target_dir, file)
        if os.path.exists(dst_file):
            if file_hash(src_file) != file_hash(dst_file):
                print("overwriting " + src_file)
                shutil.copy2(src_file, dst_file)
        else:
            print("copying " + src_file)
            shutil.copy2(src_file, dst_file)