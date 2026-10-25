import json
import pathlib
import sys
import subprocess

ROOT = pathlib.Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import run as launcher
import public_validate

def load_jsonl(path):
    if not path.exists():
        return []
    with path.open(encoding='utf-8') as f:
        return [json.loads(line) for line in f if line.strip()]

def save_jsonl(path, data):
    with path.open('w', encoding='utf-8') as f:
        for item in data:
            f.write(json.dumps(item, ensure_ascii=False) + '\n')

def main():
    inputs_dir = ROOT / 'tests' / 'inputs'
    expected_dir = ROOT / 'tests' / 'expected'
    actual_dir = ROOT / 'tests' / 'actual'
    actual_dir.mkdir(exist_ok=True)


    print("Building Java...")
    command = launcher.build('Java')

    total_fails = 0

    for inp_file in sorted(inputs_dir.glob('*.jsonl')):
        name = inp_file.stem
        exp_file = expected_dir / f'{name}.jsonl'

        requests = load_jsonl(inp_file)
        if not requests:
            continue

        print(f"\nRunning {name} ({len(requests)} cases)...")

        try:

            answers = launcher.execute(command, requests)
        except Exception as e:
            print(f"  ERROR running {name}: {e}")
            total_fails += 1
            continue


        save_jsonl(actual_dir / f'{name}.jsonl', answers)

        if not exp_file.exists():
            print(f"  WARNING: No expected file for {name}, skipping comparison.")
            continue

        expected = load_jsonl(exp_file)

        fails = 0
        for i, (req, got, want) in enumerate(zip(requests, answers, expected)):
            if not public_validate.check(req, got, want):
                print(f"  FAIL {name}[{i}]:")
                print(f"    got:  {got}")
                print(f"    want: {want}")
                fails += 1

        if fails == 0:
            print(f"  OK: {len(requests)} cases passed.")
        else:
            print(f"  FAILED: {fails} out of {len(requests)} cases.")
            total_fails += fails

    if total_fails == 0:
        print("\nALL TESTS PASSED!")
    else:
        print(f"\nTOTAL FAILURES: {total_fails}")
        sys.exit(1)

if __name__ == '__main__':
    main()