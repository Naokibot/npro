# NPointShulker 1.1.0

Spigot 1.21.1 / Java 21 向けの独立プラグインです。

## 主な機能

- `/nshulker` で購入・残高確認GUIを開く
- `/sb` を実行すると、メインハンドに持っているシュルカーボックスを直接開く
- 有効期間中は空中右クリックによる従来の開き方も利用可能
- シュルカーGUI上段27枠とプレイヤーインベントリ間で、通常クリック・Shiftクリック・ドラッグ・数字キー操作によるアイテムの出し入れが可能
- 元のシュルカーボックス自体は開いている間だけ移動・ドロップ不可
- シュルカーボックスの中に別のシュルカーボックスを入れる操作は禁止
- 1 Npointごとに現実時間3時間を追加
- 残り時間があっても追加購入可能。例: 残り2時間で購入すると残り5時間
- 有効期間中は残り時間をボスバーで常時表示
- Npoint付与は管理者コマンド `/npoint give <player> <amount>` のみ
- 残高・有効期限は `plugins/NPointShulker/data.yml` に保存

## コマンド

- `/nshulker` 購入GUI
- `/sb` メインハンドのシュルカーボックスを開く
- `/npoint give <player> <amount>` Npoint付与（管理者のみ）

## ビルド

`gradle build`

Spigot API:
`org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT`
