IF OBJECT_ID(N'[__EFMigrationsHistory]') IS NULL
BEGIN
    CREATE TABLE [__EFMigrationsHistory] (
        [MigrationId] nvarchar(150) NOT NULL,
        [ProductVersion] nvarchar(32) NOT NULL,
        CONSTRAINT [PK___EFMigrationsHistory] PRIMARY KEY ([MigrationId])
    );
END;

GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20190910142401_initial')
BEGIN
    CREATE TABLE [JBlogs] (
        [JBlogId] int NOT NULL IDENTITY,
        [Title] nvarchar(max) NULL,
        [Subject] nvarchar(max) NULL,
        [Author] nvarchar(max) NULL,
        [Url] nvarchar(max) NULL,
        CONSTRAINT [PK_JBlogs] PRIMARY KEY ([JBlogId])
    );
END;

GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20190910142401_initial')
BEGIN
    CREATE TABLE [JImages] (
        [JImageId] int NOT NULL IDENTITY,
        [Title] nvarchar(max) NULL,
        [Subject] nvarchar(max) NULL,
        [Author] nvarchar(max) NULL,
        [Url] nvarchar(max) NULL,
        CONSTRAINT [PK_JImages] PRIMARY KEY ([JImageId])
    );
END;

GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20190910142401_initial')
BEGIN
    CREATE TABLE [JPosts] (
        [JPostId] int NOT NULL IDENTITY,
        [Title] nvarchar(max) NULL,
        [Content] nvarchar(max) NULL,
        [BlogId] int NOT NULL,
        [JBlogId] int NULL,
        CONSTRAINT [PK_JPosts] PRIMARY KEY ([JPostId]),
        CONSTRAINT [FK_JPosts_JBlogs_JBlogId] FOREIGN KEY ([JBlogId]) REFERENCES [JBlogs] ([JBlogId]) ON DELETE NO ACTION
    );
END;

GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20190910142401_initial')
BEGIN
    CREATE INDEX [IX_JPosts_JBlogId] ON [JPosts] ([JBlogId]);
END;

GO

IF NOT EXISTS(SELECT * FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20190910142401_initial')
BEGIN
    INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
    VALUES (N'20190910142401_initial', N'2.1.11-servicing-32099');
END;

GO

