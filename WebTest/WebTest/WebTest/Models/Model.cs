using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace WebTest.Models
{
    public class BloggingContext : DbContext
    {
        public BloggingContext(DbContextOptions<BloggingContext> options)
            : base(options)
        { }

        public DbSet<JBlog> JBlogs { get; set; }
        public DbSet<JPost> JPosts { get; set; }
        public DbSet<JImage> JImages { get; set; }

    }

    public class JBlog
    {
        public int JBlogId { get; set; }
        public string Title { get; set; }
        public string Subject { get; set; }
        public string Author { get; set; }
        public string Url { get; set; }

        public ICollection<JPost> JPosts { get; set; }
    }

    public class JPost
    {
        public int JPostId { get; set; }
        public string Title { get; set; }
        public string Content { get; set; }

        public int BlogId { get; set; }
        public JBlog JBlog { get; set; }
    }
    public class JImage
    {
        public int JImageId { get; set; }
        public string Title { get; set; }
        public string Subject { get; set; }
        public string Author { get; set; }

        public string Url { get; set; }
    }

}
