# 커밋 푸시 가이드

다음 절차는 로컬에서 작성한 커밋을 원격 저장소에 업로드(`git push`)하는 방법을 정리한 것입니다.

## 1. 현재 브랜치와 원격 확인
```bash
git status
git branch --show-current
git remote -v
```
* `git status` 로 작업 트리를 확인하고 커밋이 존재하는지 봅니다.
* `git branch --show-current` 으로 푸시할 브랜치 이름을 확인합니다.
* `git remote -v` 로 연결된 원격 저장소가 있는지 확인합니다. 없다면 다음 단계에서 추가합니다.

## 2. 원격 저장소 추가(필요한 경우)
```bash
git remote add origin https://github.com/<사용자>/<저장소>.git
```
* 이미 `origin` 이나 원하는 이름의 원격이 있다면 이 단계는 생략합니다.
* SSH 를 사용한다면 `git@github.com:<사용자>/<저장소>.git` 형태를 사용할 수 있습니다.

## 3. 자격 증명 준비
* HTTPS 를 사용할 경우 GitHub Personal Access Token, GitLab Access Token 등 인증 수단을 준비합니다.
* SSH 를 사용할 경우 `ssh-agent` 에 키가 등록되어 있는지 확인합니다.

## 4. 푸시 실행
```bash
git push origin <브랜치명>
```
* 예를 들어 현재 브랜치가 `work` 라면 `git push origin work` 를 실행합니다.
* 최초 푸시에서 상류(upstream)를 동시에 설정하려면 `git push -u origin <브랜치명>` 을 사용할 수 있습니다.

## 5. 푸시 확인
```bash
git status
git log --oneline --decorate --graph -5
git remote show origin
```
* `git status` 로 로컬 작업 트리가 깨끗한지 확인합니다.
* `git log` 로 방금 푸시한 커밋이 있는지 확인합니다.
* `git remote show origin` 으로 추적 브랜치가 제대로 연결되었는지 확인합니다.

## 문제 해결 팁
* 인증 오류가 발생하면 토큰/SSH 키 권한을 재확인하고, `git credential-cache exit` 로 캐시를 초기화한 뒤 다시 시도합니다.
* 강제로 덮어써야 한다면 `git push --force-with-lease` 를 사용하되, 협업 중인 브랜치에서는 신중히 사용합니다.
* 네트워크가 불안정할 경우 VPN/프록시 설정을 점검하고, 재시도하기 전에 `git fetch` 로 원격 상태를 확인합니다.
