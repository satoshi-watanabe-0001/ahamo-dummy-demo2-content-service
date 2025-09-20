# シーケンス図

## 概要
ahamoダミーシステム コンテンツサービスのAPI呼び出しフローを示すシーケンス図

## キャンペーン有効性チェック API フロー

```mermaid
sequenceDiagram
    participant Client as クライアント
    participant Controller as CampaignController
    participant Service as CampaignService
    participant Repository as CampaignRepository
    participant DB as PostgreSQL

    Note over Client, DB: キャンペーン有効性チェック API (/campaigns/{id}/validity)
    
    Client->>+Controller: GET /campaigns/{id}/validity
    Note right of Client: キャンペーンID指定
    
    Controller->>Controller: パラメータ検証
    alt 無効なID形式
        Controller-->>Client: 400 Bad Request
    end
    
    Controller->>+Service: checkCampaignValidity(id)
    Note right of Controller: ビジネスロジック呼び出し
    
    Service->>+Repository: findById(id)
    Repository->>+DB: SELECT * FROM campaigns WHERE id = ?
    DB-->>-Repository: Campaign Entity or null
    Repository-->>-Service: Optional<Campaign>
    
    alt キャンペーンが存在しない場合
        Service->>Service: NOT_FOUND レスポンス作成
        Service-->>Controller: CampaignValidityResponse(isValid=false)
    else キャンペーンが存在する場合
        Service->>Service: 現在時刻取得
        Service->>Service: 有効性判定ロジック実行
        
        Note over Service: 判定条件:<br/>1. isActive = true<br/>2. validFrom <= 現在時刻 (nullの場合はスキップ)<br/>3. validUntil >= 現在時刻 (nullの場合はスキップ)
        
        alt キャンペーンが無効の場合
            Service->>Service: INACTIVE ステータス設定
        else 開始前の場合
            Service->>Service: NOT_STARTED ステータス設定
        else 期限切れの場合
            Service->>Service: EXPIRED ステータス設定
        else 有効な場合
            Service->>Service: VALID ステータス設定
        end
        
        Service->>Service: CampaignValidityResponse作成
        Service-->>-Controller: CampaignValidityResponse
    end
    
    Controller-->>-Client: 200 OK + JSON Response
    
    Note over Client: レスポンス例:<br/>{<br/>  "campaignId": "1",<br/>  "title": "春キャンペーン",<br/>  "isValid": true,<br/>  "validityStatus": "VALID",<br/>  "validFrom": "2024-03-01T00:00:00",<br/>  "validUntil": "2024-05-31T23:59:59",<br/>  "reason": "キャンペーンは有効です"<br/>}
```

## キャンペーン一覧取得 API フロー

```mermaid
sequenceDiagram
    participant Client as クライアント
    participant Controller as CampaignController
    participant Service as CampaignService
    participant Repository as CampaignRepository
    participant DB as PostgreSQL

    Note over Client, DB: キャンペーン一覧取得 API (/campaigns)
    
    Client->>+Controller: GET /campaigns?page=0&size=10
    
    Controller->>Controller: ページネーションパラメータ検証
    Controller->>+Service: getAllCampaigns(pageable)
    
    Service->>+Repository: findAll(pageable)
    Repository->>+DB: SELECT * FROM campaigns ORDER BY created_at DESC LIMIT ? OFFSET ?
    DB-->>-Repository: List<Campaign>
    Repository-->>-Service: Page<Campaign>
    
    Service->>Service: Entity → DTO変換
    loop 各キャンペーンに対して
        Service->>Service: convertToResponse(campaign)
    end
    
    Service-->>-Controller: Page<CampaignResponse>
    Controller-->>-Client: 200 OK + JSON Response
```

## キャンペーン詳細取得 API フロー

```mermaid
sequenceDiagram
    participant Client as クライアント
    participant Controller as CampaignController
    participant Service as CampaignService
    participant Repository as CampaignRepository
    participant DB as PostgreSQL

    Note over Client, DB: キャンペーン詳細取得 API (/campaigns/{id})
    
    Client->>+Controller: GET /campaigns/{id}
    
    Controller->>Controller: パラメータ検証
    Controller->>+Service: getCampaignById(id)
    
    Service->>+Repository: findById(id)
    Repository->>+DB: SELECT * FROM campaigns WHERE id = ?
    DB-->>-Repository: Campaign Entity or null
    Repository-->>-Service: Optional<Campaign>
    
    alt キャンペーンが存在しない場合
        Service-->>Controller: RuntimeException
        Controller-->>Client: 404 Not Found
    else キャンペーンが存在する場合
        Service->>Service: convertToResponse(campaign)
        Service-->>-Controller: CampaignResponse
        Controller-->>-Client: 200 OK + JSON Response
    end
```

## エラーハンドリング フロー

```mermaid
sequenceDiagram
    participant Client as クライアント
    participant Controller as Controller
    participant ExceptionHandler as GlobalExceptionHandler
    participant Service as Service
    participant DB as Database

    Note over Client, DB: エラーハンドリングの共通フロー
    
    Client->>+Controller: API Request
    Controller->>+Service: Business Logic Call
    
    alt データベースエラー
        Service->>+DB: Database Query
        DB-->>-Service: SQLException
        Service-->>-Controller: DataAccessException
        Controller->>+ExceptionHandler: Exception Handling
        ExceptionHandler-->>-Controller: ErrorResponse
        Controller-->>Client: 500 Internal Server Error
    else バリデーションエラー
        Controller->>Controller: Parameter Validation
        Controller-->>Client: 400 Bad Request
    else リソースが見つからない
        Service-->>-Controller: EntityNotFoundException
        Controller->>+ExceptionHandler: Exception Handling
        ExceptionHandler-->>-Controller: ErrorResponse
        Controller-->>Client: 404 Not Found
    else 正常処理
        Service-->>-Controller: Success Response
        Controller-->>-Client: 200 OK
    end
```

## 統合テスト フロー

```mermaid
sequenceDiagram
    participant TestClass as IntegrationTest
    participant TestContainer as Testcontainers
    participant App as SpringBootApp
    participant TestDB as TestDatabase

    Note over TestClass, TestDB: 統合テスト実行フロー
    
    TestClass->>+TestContainer: PostgreSQL Container起動
    TestContainer->>+TestDB: データベース初期化
    TestDB-->>-TestContainer: 起動完了
    TestContainer-->>-TestClass: DB接続情報
    
    TestClass->>+App: Spring Boot アプリケーション起動
    App->>TestDB: データベース接続
    App-->>-TestClass: アプリケーション起動完了
    
    loop 各テストケース
        TestClass->>TestClass: テストデータ準備
        TestClass->>App: API呼び出し
        App->>TestDB: データベース操作
        TestDB-->>App: 結果返却
        App-->>TestClass: レスポンス
        TestClass->>TestClass: アサーション実行
    end
    
    TestClass->>TestContainer: コンテナ停止
    TestContainer->>TestDB: データベース停止
```

## パフォーマンス考慮事項

### データベースアクセス最適化
1. **インデックス活用**: `is_active`, `valid_from`, `valid_until`の複合インデックス
2. **ページネーション**: LIMIT/OFFSETによる効率的なデータ取得
3. **コネクションプール**: HikariCPによる接続管理

### キャッシュ戦略
1. **キャンペーン有効性**: 短時間キャッシュ（5分）で頻繁なDB問い合わせを削減
2. **一覧データ**: ページ単位でのキャッシュ実装
3. **CDN活用**: 画像URLの配信最適化

### 監視・ログ
1. **レスポンス時間**: 各APIエンドポイントの性能監視
2. **エラー率**: 4xx/5xxエラーの発生頻度追跡
3. **データベース負荷**: スロークエリの検出と最適化
